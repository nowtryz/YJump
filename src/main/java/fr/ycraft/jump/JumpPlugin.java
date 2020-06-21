package fr.ycraft.jump;

import fr.ycraft.jump.commands.jump.JumpCommand;
import fr.ycraft.jump.commands.misc.CheckpointCommand;
import fr.ycraft.jump.commands.misc.JumpsCommand;
import fr.ycraft.jump.entity.Config;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.PlayerScore;
import fr.ycraft.jump.inventories.AbstractInventory;
import fr.ycraft.jump.inventories.JumpInventory;
import fr.ycraft.jump.manager.EditorsManager;
import fr.ycraft.jump.manager.GameManager;
import fr.ycraft.jump.manager.JumpManager;
import fr.ycraft.jump.manager.PlayerManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Optional;

/**
 * Main class of the Jump plugin, this class is loaded by Bukkit on startup
 */
public final class JumpPlugin extends JavaPlugin {
    private JumpManager jumpManager;
    private EditorsManager editorsManager;
    private GameManager gameManager;
    private Config config;
    private PlayerManager playerManager;

    @Override
    public void onEnable() {
        super.saveDefaultConfig();
        this.exportDefaultResource("fr-FR.yml");
        this.loadClasses();

        this.config = Config.fromYAML(this.getConfig(), this.getLogger());
        this.jumpManager = new JumpManager(this);
        this.editorsManager = new EditorsManager(this);
        this.gameManager = new GameManager(this);
        this.playerManager = new PlayerManager(this);

        Text.init(this);
        AbstractInventory.init();
        JumpInventory.init(this);

        this.getLogger().info(String.format(
                "Loaded the following jumps: %s",
                String.join(", ", this.jumpManager.getJumps().keySet().toArray(new String[0]))
        ));

        this.registerCommands();
        this.replacePlates();
        this.getLogger().info(String.format("%s enabled!", this.getName()));
    }

    /**
     * Preloads classes used in reflection by YAML deserializer
     */
    private void loadClasses() {
        try {
            this.getClassLoader().loadClass(Jump.class.getName());
            this.getClassLoader().loadClass(PlayerScore.class.getName());
        } catch (ClassNotFoundException e) {
            this.getLogger().severe("Unable to preload classes for Yaml deserialization");
        }
    }

    /**
     * Register all Jump plugin commands
     */
    private void registerCommands() {
        new JumpCommand(this);
        new JumpsCommand(this);
        new CheckpointCommand(this);
    }

    private void replacePlates() {
        this.jumpManager.getJumps().values().forEach(jump -> {
            // Place blocks bellow plates
            jump.getStart()
                    .map(Location::getBlock)
                    .map(block -> block.getRelative(BlockFace.DOWN))
                    .filter(b -> !b.getType().isOccluding())
                    .ifPresent(b -> b.setType(Material.GOLD_BLOCK));
            jump.getEnd()
                    .map(Location::getBlock)
                    .map(block -> block.getRelative(BlockFace.DOWN))
                    .filter(b -> !b.getType().isOccluding())
                    .ifPresent(b -> b.setType(Material.GOLD_BLOCK));
            jump.getCheckpoints()
                    .stream()
                    .map(Location::getBlock)
                    .map(block -> block.getRelative(BlockFace.DOWN))
                    .filter(b -> !b.getType().isOccluding())
                    .forEach(b -> b.setType(Material.GOLD_BLOCK));
            // Place plates
            jump.getStart()
                    .map(Location::getBlock)
                    .filter(b -> !b.getType().equals(this.config.getStartMaterial()))
                    .ifPresent(b -> b.setType(this.config.getStartMaterial()));
            jump.getEnd()
                    .map(Location::getBlock)
                    .filter(b -> !b.getType().equals(this.config.getEndMaterial()))
                    .ifPresent(b -> b.setType(this.config.getEndMaterial()));
            jump.getCheckpoints()
                    .stream()
                    .map(Location::getBlock)
                    .filter(b -> !b.getType().equals(this.config.getCheckpointMaterial()))
                    .forEach(b -> b.setType(this.config.getCheckpointMaterial()));
        });
    }

    @Override
    public void onDisable() {
        Optional.ofNullable(this.editorsManager).ifPresent(EditorsManager::close);
        Optional.ofNullable(this.jumpManager).ifPresent(JumpManager::save);
        Optional.ofNullable(this.gameManager).ifPresent(GameManager::stopAll);
    }

    /**
     * Saves the raw contents of any resource embedded with a plugin's .jar file assuming it can be found using
     * {@link JavaPlugin#getResource(String)} if the file doesn't exist. The resource is saved into the plugin's data
     * folder using the same hierarchy as the .jar file (subdirectories are preserved).
     * @param fileName the resource to export
     */
    private void exportDefaultResource(String fileName) {
        File file = new File(getDataFolder(), fileName);
        File parent = file.getParentFile();

        if (!parent.exists() && !parent.mkdirs()) {
            this.getLogger().severe("Unable to create " + parent + "directory");
            return;
        }

        if (!file.exists()) saveResource(fileName, false);
        else if (file.isDirectory() && file.delete()) saveResource(fileName, false);
    }

    public Config getConfigProvider() {
        return config;
    }

    public EditorsManager getEditorsManager() {
        return editorsManager;
    }

    public JumpManager getJumpManager() {
        return jumpManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
