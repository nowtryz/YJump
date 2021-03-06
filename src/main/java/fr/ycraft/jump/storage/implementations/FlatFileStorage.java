package fr.ycraft.jump.storage.implementations;

import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.google.mu.util.stream.BiStream;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.*;
import fr.ycraft.jump.util.JumpCollector;
import net.nowtryz.mcutils.exceptions.StreamException;
import net.nowtryz.mcutils.injection.DataFolder;
import net.nowtryz.mcutils.injection.PluginLogger;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FlatFileStorage implements StorageImplementation {
    private final Map<UUID, ReentrantLock> locks = new HashMap<>();
    private Configuration jumpDefaults = null;
    private final JumpPlugin plugin;
    private final File playersFolder;
    private final File jumpsFolder;
    private final Logger logger;

    @Inject
    FlatFileStorage(JumpPlugin plugin, @PluginLogger Logger logger, @DataFolder File dataFolder) {
        this.plugin = plugin;
        this.logger = logger;
        this.playersFolder = new File(dataFolder, "players");
        this.jumpsFolder = new File(dataFolder, "jumps");
    }

    @Override
    public void init() throws IllegalStateException, IOException {
        try {
            // Preloads classes used in reflection by YAML deserializer
            JumpPlugin.class.getClassLoader().loadClass(Jump.class.getName());
            JumpPlugin.class.getClassLoader().loadClass(PlayerScore.class.getName());
            JumpPlugin.class.getClassLoader().loadClass(Position.class.getName());
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().severe("[YJump] Unable to preload classes for Yaml deserialization");
            throw new ExceptionInInitializerError(e);
        }

        if (this.playersFolder.mkdirs()) plugin.getLogger().info("Created a fresh new score folder");
        try (
                final InputStream defConfigStream = this.plugin.getResource("jump.defaults.yml");
                final InputStreamReader reader = new InputStreamReader(defConfigStream, Charsets.UTF_8)
        ) {
            Validate.notNull(defConfigStream, "How the hell, the default jump file can be missing in the jar");
            this.jumpDefaults = YamlConfiguration.loadConfiguration(reader);

        } catch (IOException | IllegalStateException e) {
            throw new IllegalStateException("Unable to load default jump data", e);
        }

        if(this.jumpsFolder.isFile()) throw new IllegalStateException("The jumps folder is a file");
        if(this.playersFolder.isFile()) throw new IllegalStateException("The players folder is a file");

        if (this.jumpsFolder.mkdirs()) this.logger.info("Created new jumps folder");
        if (this.playersFolder.mkdirs()) this.logger.info("Created new players folder");
    }

    /**
     * There isn't any connection to close or anything, so the method doesn't do anything
     */
    @Override
    public void close() {

    }

    private ReentrantLock getPlayerLock(UUID uuid) {
        return this.locks.computeIfAbsent(uuid, id -> new ReentrantLock());
    }

    private File getPlayerFile(OfflinePlayer player) {
        return this.getPlayerFile(player.getUniqueId());
    }

    private File getPlayerFile(UUID id) {
        return new File(this.playersFolder, id + ".yml");
    }

    public File getJumpFile(Jump jump) {
        return this.getJumpFile(jump.getId());
    }

    public File getJumpFile(UUID id) {
        return new File(this.jumpsFolder, id.toString() + ".yml");
    }

    @Override
    public JumpPlayer loadPlayer(OfflinePlayer player) throws IOException {
        this.getPlayerLock(player.getUniqueId()).lock();
        File file = this.getPlayerFile(player);

        try {
            if (!file.exists() && file.createNewFile()) {
                this.plugin.getLogger().info(String.format("Created a new score file for %s", player.getName()));
            }

            YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
            return new JumpPlayer(player.getUniqueId(), player.getName(), BiStream.from(this.plugin.getJumpManager().getJumpsById())
                    .inverse()
                    .mapValues(UUID::toString)
                    .mapValues(conf::getLongList)
                    .mapValues(list -> list.parallelStream()
                            .map(TimeScore::new)
                            .collect(Collectors.toList()))
                    .toMap());
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Unable to load scores file", exception);
            throw exception;
        } finally {
            this.getPlayerLock(player.getUniqueId()).unlock();
        }
    }

    @Override
    public List<JumpPlayer> loadAllPlayers() throws Exception {
        if (!this.playersFolder.exists() || this.playersFolder.list().length == 0) return new ArrayList<>();
        try {
            return Arrays.stream(this.playersFolder.list())
                    .parallel()
                    .filter(s -> s.endsWith(".yml"))
                    .map(s -> s.substring(0, s.lastIndexOf('.')))
                    .map(UUID::fromString)
                    .map(Bukkit::getOfflinePlayer)
                    .map(StreamException.warp(this::loadPlayer))
                    .collect(Collectors.toList());
        } catch (StreamException exception) {
            throw exception.getCause();
        }
    }

    @Override
    public void storePlayer(JumpPlayer jumpPlayer) throws Exception {
        this.getPlayerLock(jumpPlayer.getId()).lock();
        File file = this.getPlayerFile(jumpPlayer.getId());

        try {
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
            BiStream.from(jumpPlayer)
                    .mapKeys(Jump::getId)
                    .mapKeys(UUID::toString)
                    .mapValues(scores -> scores.parallelStream()
                            .map(TimeScore::getDuration)
                            .collect(Collectors.toList()))
                    .forEach(conf::set);
            conf.save(file);
        } catch (IOException exception) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to save scores", exception);
            throw exception;
        } finally {
            this.getPlayerLock(jumpPlayer.getId()).unlock();
        }
    }

    @Override
    public boolean deletePlayer(UUID id) {
        return this.getPlayerFile(id).delete();
    }

    @Override
    public List<Jump> loadJumps() {
        return Arrays
                .stream(this.jumpsFolder.listFiles((dir, name) -> name.endsWith(".yml")))
                .filter(File::isFile)
                .map(YamlConfiguration::loadConfiguration)
                .peek(conf -> conf.setDefaults(this.jumpDefaults))
                .peek(conf -> conf.options().copyDefaults(true))
                .map(conf -> conf.get("jump"))
                .filter(Jump.class::isInstance)
                .map(Jump.class::cast)
                .collect(JumpCollector.toList(
                        name -> this.plugin.getLogger().severe(
                                "Jump \"" + name + "\" is duplicated, please verify your jump files"),
                        Jump::getName,
                        Function.identity()
                ));
    }

    @Override
    public void storeJump(Jump jump) throws Exception {
        File file = this.getJumpFile(jump);

        try {
            if ( !file.exists() && file.createNewFile()) this.plugin.getLogger().info(String.format("Created %s", file.getName()));

            YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
            conf.setDefaults(this.jumpDefaults);
            conf.options().copyDefaults(true);
            conf.set("jump", jump);
            conf.save(file);
        } catch (IOException exception) {
            this.logger.log(Level.SEVERE, String.format("Unable to save %s:", file.getName()), exception);
            throw exception;
        }
    }

    @Override
    public boolean deleteJump(UUID id) {
        File file = this.getJumpFile(id);
        if (!file.delete()) {
            this.plugin.getLogger().severe(() -> "Unable to delete " + file.getName());
            return false;
        }
        return true;
    }
}
