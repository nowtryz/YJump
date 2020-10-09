package fr.ycraft.jump;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.mysql.jdbc.Driver;
import fr.ycraft.jump.commands.jump.JumpCommand;
import fr.ycraft.jump.commands.misc.CheckpointCommand;
import fr.ycraft.jump.commands.misc.JumpsCommand;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.PlayerScore;
import fr.ycraft.jump.injection.JumpModule;
import fr.ycraft.jump.inventories.JumpInventory;
import fr.ycraft.jump.listeners.InventoryListener;
import fr.ycraft.jump.listeners.PlateListener;
import fr.ycraft.jump.listeners.PlatesProtectionListener;
import fr.ycraft.jump.listeners.PlayerListener;
import fr.ycraft.jump.manager.EditorsManager;
import fr.ycraft.jump.manager.GameManager;
import fr.ycraft.jump.manager.JumpManager;
import fr.ycraft.jump.manager.PlayerManager;
import fr.ycraft.jump.storage.Storage;
import fr.ycraft.jump.util.ItemLibrary;
import fr.ycraft.jump.util.MetricsUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Inject;
import java.io.File;
import java.util.Optional;

/**
 * Main class of the Jump plugin, this class is loaded by Bukkit on startup
 */
@Getter(onMethod_={@Deprecated})
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

    // managers and listeners
    private @Inject Config configProvider;
    private @Inject EditorsManager editorsManager;
    private @Inject GameManager gameManager;
    private @Inject InventoryListener inventoryListener;
    private @Inject Storage storage;
    private @Inject JumpManager jumpManager;
    private @Inject PlayerManager playerManager;

    // commands
    private @Inject JumpCommand jumpCommand;
    private @Inject JumpsCommand jumpsCommand;

    @Getter
    private Injector injector;
    @Getter
    private boolean enabling = false, disabling = false, prod;

    @Override
    public void onEnable() {
        this.enabling = true;
        super.saveDefaultConfig();
        this.exportDefaultResource("fr-FR.yml");

        this.prod = !this.getDescription().getVersion().contains("SNAPSHOT");
        this.injector = Guice.createInjector(isProd() ? Stage.PRODUCTION : Stage.DEVELOPMENT, new JumpModule(this));
        injector.injectMembers(this);

        Text.init(this);
        ItemLibrary.init();
        JumpInventory.init(this);
        MetricsUtils.init(this);

        this.storage.init();
        this.playerManager.init();
        this.jumpManager.init();

        String[] jumps = this.jumpManager
                .getJumps()
                .keySet()
                .toArray(new String[0]);
        if (jumps.length == 0) this.getLogger().warning("No jump loaded");
        else this.getLogger().info(String.format(
                "Loaded the following jumps: %s",
                String.join(", ", jumps)
        ));

        this.registerCommands();
        this.replacePlates();
        this.getLogger().info(String.format("%s enabled!", this.getName()));
        this.enabling = false;
    }

    /**
     * Register all Jump plugin commands
     */
    private void registerCommands() {
//        new JumpCommand(this);
//        new JumpsCommand(this);
        new CheckpointCommand(this);
    }

    @Inject
    private void registerListeners(
            PlateListener plateListener,
            PlayerListener playerListener,
            PlatesProtectionListener platesProtectionListener) {
        plateListener.register();
        playerListener.register();
        platesProtectionListener.register();
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
                    .filter(b -> !b.getType().equals(this.configProvider.get(Key.START_MATERIAL)))
                    .ifPresent(b -> b.setType(this.configProvider.get(Key.START_MATERIAL)));
            jump.getEnd()
                    .map(Location::getBlock)
                    .filter(b -> !b.getType().equals(this.configProvider.get(Key.END_MATERIAL)))
                    .ifPresent(b -> b.setType(this.configProvider.get(Key.END_MATERIAL)));
            jump.getCheckpoints()
                    .stream()
                    .map(Location::getBlock)
                    .filter(b -> !b.getType().equals(this.configProvider.get(Key.CHECKPOINT_MATERIAL)))
                    .forEach(b -> b.setType(this.configProvider.get(Key.CHECKPOINT_MATERIAL)));
        });
    }

    @Override
    public void onDisable() {
        this.disabling = true;
        Optional.ofNullable(this.editorsManager).ifPresent(EditorsManager::close);
        Optional.ofNullable(this.jumpManager).ifPresent(JumpManager::save);
        Optional.ofNullable(this.gameManager).ifPresent(GameManager::stopAll);
        Optional.ofNullable(this.storage).ifPresent(Storage::close);
        this.disabling = false;
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

    public boolean isReady() {
        return !this.enabling && !this.disabling && this.isEnabled();
    }
}
