package fr.ycraft.jump.manager;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

public class ScoresManager extends AbstractManager {
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<String, FileConfiguration> loadedFiles = new HashMap<>();
    private final File scoresFolder;

    public ScoresManager(JumpPlugin plugin) {
        super(plugin);

        this.scoresFolder = new File(plugin.getDataFolder(), "scores");
        if (this.scoresFolder.mkdirs()) plugin.getLogger().info("Created a fresh new score folder");
    }

    public void saveNewPlayerScore(Player player, Jump jump, long millis) {

    }

    private FileConfiguration loadJumpScores(Jump jump) {
        this.lock.readLock().lock();
        File file = this.getFile(jump);

        try {
            if (!file.exists() && file.createNewFile()) {
                this.plugin.getLogger().info(String.format("Created a new score file for %s", jump.getName()));
            }
        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Unable to load scores file", exception);
        }

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
        this.loadedFiles.put(jump.getName(), conf);
        this.lock.readLock().unlock();
        return conf;
    }

    private void unloadJump(Jump jump) {
        Optional.ofNullable(this.loadedFiles.get(jump.getName()))
            .ifPresent(conf -> {
                this.lock.writeLock().lock();
                try {
                    conf.save(this.getFile(jump));
                } catch (IOException exception) {
                    this.plugin.getLogger().log(Level.SEVERE, "Unable to save scores", exception);
                } finally {
                    this.loadedFiles.remove(jump.getName());
                    this.lock.writeLock().unlock();
                }
            });
    }

    private File getFile(Jump jump) {
        return new File(this.scoresFolder, jump.getName());
    }
}
