package fr.ycraft.jump;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.mysql.jdbc.Driver;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.PlayerScore;
import fr.ycraft.jump.entity.Position;
import fr.ycraft.jump.enums.Patterns;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.exceptions.ParkourException;
import net.nowtryz.mcutils.injection.CommandModule;
import fr.ycraft.jump.injection.JumpModule;
import fr.ycraft.jump.injection.TemplatesModule;
import fr.ycraft.jump.listeners.PlateListener;
import fr.ycraft.jump.listeners.PlatesProtectionListener;
import fr.ycraft.jump.listeners.PlayerListener;
import fr.ycraft.jump.manager.EditorsManager;
import fr.ycraft.jump.manager.GameManager;
import fr.ycraft.jump.manager.JumpManager;
import fr.ycraft.jump.manager.PlayerManager;
import fr.ycraft.jump.storage.Storage;
import fr.ycraft.jump.util.MetricsUtils;
import lombok.Getter;
import net.nowtryz.mcutils.api.Plugin;
import net.nowtryz.mcutils.api.listener.InventoryListener;
import net.nowtryz.mcutils.command.CommandManager;
import net.nowtryz.mcutils.command.CommandResult;
import net.nowtryz.mcutils.command.contexts.ExecutionContext;
import net.nowtryz.mcutils.injection.BukkitModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;

/**
 * Main class of the Jump plugin, this class is loaded by Bukkit on startup
 */
@Getter(onMethod_={@Deprecated})
public final class JumpPlugin extends JavaPlugin implements Plugin {
    /*
     * Preloads classes used in reflection by YAML deserializer
     */
    static {
        try {
            JumpPlugin.class.getClassLoader().loadClass(Jump.class.getName());
            JumpPlugin.class.getClassLoader().loadClass(PlayerScore.class.getName());
            JumpPlugin.class.getClassLoader().loadClass(Driver.class.getName());
            JumpPlugin.class.getClassLoader().loadClass(Position.class.getName());
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
    private @Inject CommandManager commandManager;

    private @Getter Injector injector;
    private @Getter boolean enabling = false;
    private @Getter boolean disabling = false;
    private @Getter boolean prod;

    @Override
    public void onEnable() {
        this.enabling = true;
        this.prod = !this.getDescription().getVersion().contains("SNAPSHOT");

        try {
            super.saveDefaultConfig();
            this.exportResources();

            // This will create the injector and inject all required objects to the plugin
            this.injector = Guice.createInjector(
                    isProd() ? Stage.PRODUCTION : Stage.DEVELOPMENT,
                    new JumpModule(),
                    new CommandModule(),
                    new BukkitModule<>(this, JumpPlugin.class),
                    new TemplatesModule(this)
            );

            Text.init(this);
            MetricsUtils.init(this);
            Jump.setDefaultMaterial(this.configProvider.get(Key.DEFAULT_JUMP_ICON));

            this.commandManager.initDefaults(this::handleCommandResult);
            this.commandManager.printGraph();

            this.storage.init();
            this.jumpManager.init();
            this.playerManager.init();

            this.showJumpList();
            this.replacePlates();
            this.getLogger().info(String.format("%s enabled!", this.getName()));
            this.enabling = false;

        } catch (RuntimeException | ParkourException exception) {
            this.enabling = false;
            this.getLogger().severe("Unable to enable the plugin:" + exception.getMessage());
            if (!this.prod) exception.printStackTrace();
            this.getLogger().warning("Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void handleCommandResult(ExecutionContext context, CommandResult result) {
        context.getSender().sendMessage(result.toString());
    }

    /**
     * Register permanent plugin listeners
     * @param plateListener game trigger
     * @param playerListener player join/quit
     * @param platesProtectionListener plates protection
     */
    @Inject
    public void registerListeners(
            PlateListener plateListener,
            PlayerListener playerListener,
            PlatesProtectionListener platesProtectionListener) {
        plateListener.register();
        playerListener.register();
        platesProtectionListener.register();
    }

    private void showJumpList() {
        String[] jumps = this.jumpManager
                .getJumps()
                .keySet()
                .toArray(new String[0]);
        if (jumps.length == 0) this.getLogger().warning("No jump loaded");
        else this.getLogger().info(String.format(
                "Loaded the following jumps: %s",
                String.join(", ", jumps)
        ));

        this.jumpManager
                .getJumps()
                .values()
                .parallelStream()
                .filter(jump -> jump.getWorld() == null)
                .forEach(jump -> this.getLogger().warning(String.format(
                        "The world of `%1$s` is not set or have changed, " +
                                "please update it with `/jump setworld %1$s <world>`",
                        jump.getName()
                )));
    }

    public void replacePlates() {
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
        this.getLogger().info("Closing editors...");
        Optional.ofNullable(this.editorsManager).ifPresent(EditorsManager::close);
        this.getLogger().info("Saving players...");
        Optional.ofNullable(this.playerManager).ifPresent(PlayerManager::save);
        this.getLogger().info("Saving jumps...");
        Optional.ofNullable(this.jumpManager).ifPresent(JumpManager::save);
        this.getLogger().info("Stopping running games...");
        Optional.ofNullable(this.gameManager).ifPresent(GameManager::stopAll);
        this.getLogger().info("Closing storage...");
        Optional.ofNullable(this.storage).ifPresent(Storage::close);
        this.disabling = false;
    }

    private void exportResources() {
        Arrays.stream(Text.AVAILABLE_LOCALES).map(Text::localeToFileName).forEach(this::exportDefaultResource);
        Arrays.stream(Patterns.values())
                .map(Patterns::getFileName)
                .map(s -> Patterns.FOLDER_NAME + '/' + s)
                .forEach(this::exportDefaultResource);
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
