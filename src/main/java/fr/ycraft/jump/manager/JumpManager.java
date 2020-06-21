package fr.ycraft.jump.manager;

import com.google.common.base.Charsets;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
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
    private final Map<String, Jump> jumps;

    public JumpManager(JumpPlugin plugin) {
        super(plugin);
        Map<String, Jump> jumps1 = new LinkedHashMap<>();;

        this.jumpsFolder = new File(plugin.getDataFolder(), "jumps");
        if (!this.jumpsFolder.mkdirs()) {
            try (
                final InputStream defConfigStream = this.plugin.getResource("jump.defaults.yml");
                final InputStreamReader reader = new InputStreamReader(defConfigStream, Charsets.UTF_8)
            ) {
                // Validate.notNull(defConfigStream, "How the hell, the default jump file can be missing in the jar");
                YamlConfiguration defaults = YamlConfiguration.loadConfiguration(reader);

                jumps1 = Arrays.stream(this.jumpsFolder.listFiles(this::filter))
                    .map(YamlConfiguration::loadConfiguration)
                    .peek(c -> c.setDefaults(defaults))
                    .peek(c -> c.options().copyDefaults(true))
                    .map(this::loadJump)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Jump::getName, Function.identity()));
            } catch (IOException e) {
                this.plugin.getLogger().severe("Unable to load default jump data");
            } catch (IllegalStateException e) {
                this.plugin.getLogger().severe("Unable to load default jump data: " + e.getMessage());
                this.plugin.getLogger().severe("Did you manually edit jump files?");
            }
        }

        this.jumps = jumps1;
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

    // Inner logic

    private Jump loadJump(YamlConfiguration conf) {
        return Optional.ofNullable(conf.get("jump"))
                .filter(Jump.class::isInstance)
                .map(Jump.class::cast)
                .orElse(null);
    }

    private boolean filter(File dir, String fileName) {
        if (!fileName.endsWith(".yml") && !fileName.endsWith(".yaml")) return false;
        return !new File(dir, fileName).isDirectory();
    }
}
