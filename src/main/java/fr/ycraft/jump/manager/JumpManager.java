package fr.ycraft.jump.manager;

import com.google.common.base.Charsets;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import org.apache.commons.lang3.Validate;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JumpManager extends AbstractManager {
    private final File jumpsFolder;
    private final Configuration defaults;
    private final Map<String, Jump> jumps;

    public JumpManager(JumpPlugin plugin) {
        super(plugin);
        Configuration defaults = null;

        this.jumpsFolder = new File(plugin.getDataFolder(), "jumps");
        if (!this.jumpsFolder.mkdirs()) {
            try (
                final InputStream defConfigStream = this.plugin.getResource("jump.defaults.yml");
                final InputStreamReader reader = new InputStreamReader(defConfigStream, Charsets.UTF_8)
            ) {
                Validate.notNull(defConfigStream, "How the hell, the default jump file can be missing in the jar");
                defaults = YamlConfiguration.loadConfiguration(reader);

            } catch (IOException e) {
                this.plugin.getLogger().severe("Unable to load default jump data");
            } catch (IllegalStateException e) {
                this.plugin.getLogger().severe(() -> "Unable to load jump data: " + e.getMessage());
                this.plugin.getLogger().severe("Did you manually edit jump files?");
            }
        }

        this.defaults = defaults;
        this.jumps = Arrays.stream(this.jumpsFolder.listFiles((dir, name) ->
                (name.endsWith(".yml") || name.endsWith(".yaml"))
                        && !new File(dir, name).isDirectory()))
                .map(YamlConfiguration::loadConfiguration)
                .peek(conf -> conf.setDefaults(this.defaults))
                .peek(conf -> conf.options().copyDefaults(true))
                .map(conf -> conf.get("jump"))
                .filter(Jump.class::isInstance)
                .map(Jump.class::cast)
                .collect(Collectors.toMap(Jump::getName, Function.identity()));
    }

    // Getters

    public Map<String, Jump> getJumps() {
        return this.jumps;
    }

    public Optional<Jump> getJump(String name) {
        return Optional.ofNullable(this.jumps.get(name));
    }

    public File getFile(Jump jump) {
        return new File(this.jumpsFolder, jump.getName() + ".yml");
    }

    // Utilities

    public void persist(Jump jump) {
        this.jumps.put(jump.getName(), jump);
        File file = new File(this.jumpsFolder, jump.getName() + ".yml");

        try {
            if ( !file.exists() && file.createNewFile()) this.plugin.getLogger().info(String.format("Created %s", file.getName()));

            YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
            conf.setDefaults(this.defaults);
            conf.options().copyDefaults(true);
            conf.set("jump", jump);
            conf.save(file);
        } catch (IOException exception) {
            this.plugin.getLogger().severe(String.format("Unable to save %s: %s", file.getName(), exception.getMessage()));
        }
    }

    public void delete(Jump jump) {
        this.jumps.remove(jump.getName());
        this.plugin.getGameManager().updateJumpList();
        File file = this.getFile(jump);
        if (!file.delete()) this.plugin.getLogger().severe("Unable to delete " + file.getName());
    }

    public void save() {
        this.jumps.values().forEach(this::persist);
    }
}
