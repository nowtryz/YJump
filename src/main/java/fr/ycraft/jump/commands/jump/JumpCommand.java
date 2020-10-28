package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.sessions.JumpGame;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.commands.PluginCommandExecutor;
import fr.ycraft.jump.manager.EditorsManager;
import fr.ycraft.jump.manager.GameManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.List;

public class JumpCommand extends PluginCommandExecutor {
    private @Inject EditorsManager editorsManager;
    private @Inject GameManager gameManager;

    @Inject
    public JumpCommand(@NotNull JumpPlugin plugin,
                       AddCheckPointCommand addCheckPointCommand,
                       CreateCommand createCommand,
                       DeleteCommand deleteCommand,
                       EditCommand editCommand,
                       HelpCommand helpCommand,
                       InfoCommand infoCommand,
                       LeaveCommand leaveCommand,
                       ListCommand listCommand,
                       ReloadCommand reloadCommand,
                       RenameCommand renameCommand,
                       SaveCommand saveCommand,
                       SetDescriptionCommand setDescriptionCommand,
                       SetEndCommand setEndCommand,
                       SetItemCommand setItemCommand,
                       SetSpawnCommand setSpawnCommand,
                       SetStartCommand setStartCommand,
                       SetWorldCommand setWorldCommand
    ) {
        super(plugin, CommandSpec.JUMP, any -> true, // allow any arguments to show help message
                addCheckPointCommand,
                createCommand,
                deleteCommand,
                editCommand,
                helpCommand,
                infoCommand,
                leaveCommand,
                listCommand,
                reloadCommand,
                renameCommand,
                saveCommand,
                setDescriptionCommand,
                setEndCommand,
                setItemCommand,
                setSpawnCommand,
                setStartCommand,
                setWorldCommand);
    }

    @Override
    public boolean execute(JumpPlugin plugin, CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0) {
                Text.UNKNOWN_COMMAND.send(player);
            } else if (this.editorsManager.isInEditor(player)) {
                this.editorsManager.leave(player);
            } else if (this.gameManager.isPlaying(player)) {
                this.gameManager.getGame(player).ifPresent(JumpGame::close);
            } else {
                HelpCommand.sendHelp(player);
            }
        } else {
            Text.UNKNOWN_COMMAND.send(sender);
        }

        return true;
    }

    @Override
    public List<String> tabComplete(JumpPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
