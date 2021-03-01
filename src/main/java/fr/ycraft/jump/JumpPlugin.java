package fr.ycraft.jump;

import com.google.inject.Guice;
import com.google.inject.Stage;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.enums.Patterns;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.exceptions.ParkourException;
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
import net.nowtryz.mcutils.api.listener.GuiListener;
import net.nowtryz.mcutils.command.CommandManager;
import net.nowtryz.mcutils.command.CommandResult;
import net.nowtryz.mcutils.command.SenderType;
import net.nowtryz.mcutils.command.contexts.ExecutionContext;
import net.nowtryz.mcutils.injection.BukkitModule;
import net.nowtryz.mcutils.injection.CommandModule;
import net.nowtryz.mcutils.inventory.GuiModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
    // managers and listeners
    private @Inject Config configProvider;
    private @Inject EditorsManager editorsManager;
    private @Inject GameManager gameManager;
    private @Inject GuiListener inventoryListener;
    private @Inject Storage storage;
    private @Inject JumpManager jumpManager;
    private @Inject PlayerManager playerManager;
    private @Inject CommandManager commandManager;

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
            Guice.createInjector(
                    isProd() ? Stage.PRODUCTION : Stage.DEVELOPMENT,
                    new JumpModule(),
                    new CommandModule(),
                    new GuiModule(),
                    new BukkitModule<>(this, JumpPlugin.class),
                    new TemplatesModule(this)
            );

            Text.init(this);
            MetricsUtils.init(this);
            Jump.setDefaultMaterial(this.configProvider.get(Key.DEFAULT_JUMP_ICON));

            this.commandManager.initDefaults(this::handleCommandResult);
//            this.commandManager.getCommands()
//                    .stream()
//                    .map(Execution::getCommand)
//                    .forEach(this.getLogger()::info);

            this.storage.init();
            this.jumpManager.init();
            this.playerManager.init();

            this.jumpManager.replacePlates();
            this.getLogger().info(String.format("%s enabled!", this.getName()));

        } catch (RuntimeException | ParkourException exception) {
            this.enabling = false;
            this.getLogger().severe("Unable to enable the plugin:" + exception.getMessage());
            if (!this.prod) exception.printStackTrace();
            this.getLogger().warning("Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
        } finally {
            this.enabling = false;
        }
    }

    private void handleCommandResult(ExecutionContext context, CommandResult result) {
        switch (result) {
            case WRONG_TARGET:
                if (context.getTarget() == SenderType.PLAYER) {
                    context.reply("§cYou must a player to perform this command");
                } else if (context.getSender() instanceof Player) {
                    context.reply("§cThis command cannot be executed by a player");
                } else {
                    context.reply("§cThis command cannot be executed by the current entity");
                }
                break;
            case INTERNAL_ERROR:
                context.reply(Text.COMMAND_ERROR);
                break;
            case INVALID_ARGUMENTS:
                context.reply("§cInvalid arguments supplied to the command");
                break;
            case MISSING_PERMISSION:
                context.reply(Text.NO_PERM);
                break;
            case NOT_IMPLEMENTED:
                context.reply("§cThis command has not been implemented yep");
                break;
            case UNKNOWN:
                context.reply(Text.UNKNOWN_COMMAND);
                break;
            default:
                break;
        }
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
