package fr.ycraft.jump;

import com.mysql.jdbc.Driver;
import fr.ycraft.jump.commands.jump.JumpCommand;
import fr.ycraft.jump.commands.misc.CheckpointCommand;
import fr.ycraft.jump.commands.misc.JumpsCommand;
import fr.ycraft.jump.entity.Config;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.PlayerScore;
import fr.ycraft.jump.inventories.JumpInventory;
import fr.ycraft.jump.listeners.InventoryListener;
import fr.ycraft.jump.listeners.PlateListener;
import fr.ycraft.jump.listeners.PlatesProtectionListener;
import fr.ycraft.jump.manager.EditorsManager;
import fr.ycraft.jump.manager.GameManager;
import fr.ycraft.jump.manager.JumpManager;
import fr.ycraft.jump.manager.PlayerManager;
import fr.ycraft.jump.manager.database.DBJumpManager;
import fr.ycraft.jump.manager.database.DBPlayerManager;
import fr.ycraft.jump.manager.file.FileJumpManager;
import fr.ycraft.jump.manager.file.FilePlayerManager;
import fr.ycraft.jump.util.DatabaseUtil;
import fr.ycraft.jump.util.ItemLibrary;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Main class of the Jump plugin, this class is loaded by Bukkit on startup
 */
public final class JumpPlugin extends JavaPlugin {
    /*
     * Preloads classes used in reflection by YAML deserializer
     */
    static {
        try {
            JumpPlugin.class.getClassLoader().loadClass(Jump.class.getName());
            JumpPlugin.class.getClassLoader().loadClass(PlayerScore.class.getName());
            JumpPlugin.class.getClassLoader().loadClass(Driver.class.getName());
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().severe("[YJump] Unable to preload classes for Yaml deserialization");
        }
    }

    private Config config;
    private JumpManager jumpManager;
    private EditorsManager editorsManager;
    private GameManager gameManager;
    private PlayerManager playerManager;
    private InventoryListener inventoryListener;
    private boolean isDisabling = false;

    @Override
    public void onEnable() {
        super.saveDefaultConfig();
        this.exportDefaultResource("fr-FR.yml");

        this.config = new Config(this.getConfig(), this.getLogger());
        this.editorsManager = new EditorsManager(this);
        this.gameManager = new GameManager(this);
        this.inventoryListener = new InventoryListener(this);

        if (this.config.isDatabaseStorage()) {
            try {
                this.getLogger().info(String.format(
                    "Using %s database on %s:%d",
                    this.config.getDatabaseName(),
                    this.config.getDatabaseHost(),
                    this.config.getDatabasePort()
                ));
                this.initDatabaseManagers();
            } catch (SQLException exception) {
                this.initFileManagers();
                this.getLogger().severe("Unable to connect to database: " + exception);
                this.getLogger().warning("Falling back to yaml files");
                this.getLogger().log(Level.SEVERE, "Stack trace:", exception);
            }
        } else {
            this.initFileManagers();
            this.getLogger().info("Using YAML files");
        }

        Text.init(this);
        ItemLibrary.init();
        JumpInventory.init(this);

        String[] jumps = this.jumpManager.getJumps().keySet().toArray(new String[0]);
        if (jumps.length == 0) this.getLogger().warning("No jump loaded");
        else this.getLogger().info(String.format(
                "Loaded the following jumps: %s",
                String.join(", ", jumps)
        ));

        this.registerCommands();
        this.registerListeners();
        this.replacePlates();
        this.getLogger().info(String.format("%s enabled!", this.getName()));
    }

    /**
     * Register all Jump plugin commands
     */
    private void registerCommands() {
        new JumpCommand(this);
        new JumpsCommand(this);
        new CheckpointCommand(this);
    }

    private void registerListeners() {
        new PlateListener(this).register();
        new PlatesProtectionListener(this).register();
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

    private void initFileManagers() {
        this.jumpManager = new FileJumpManager(this);
        this.playerManager = new FilePlayerManager(this);
    }

    private void initDatabaseManagers() throws SQLException {
        this.jumpManager = new DBJumpManager(this);
        this.playerManager = new DBPlayerManager(this);
    }

    @Override
    public void onDisable() {
        this.isDisabling = true;
        Optional.ofNullable(this.editorsManager).ifPresent(EditorsManager::close);
        Optional.ofNullable(this.jumpManager).ifPresent(JumpManager::save);
        Optional.ofNullable(this.gameManager).ifPresent(GameManager::stopAll);
        this.isDisabling = false;
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

    public Config getConfigProvider() { return config; }
    public EditorsManager getEditorsManager() { return editorsManager; }
    public JumpManager getJumpManager() { return jumpManager; }
    public GameManager getGameManager() { return gameManager; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public InventoryListener getInventoryListener() { return inventoryListener; }

    public boolean isDisabling() { return isDisabling; }
}
