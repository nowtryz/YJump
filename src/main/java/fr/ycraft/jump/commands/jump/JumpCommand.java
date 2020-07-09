package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.JumpGame;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.commands.PluginCommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JumpCommand extends PluginCommandExecutor {
    public JumpCommand(@NotNull JumpPlugin plugin) {
        super(plugin, CommandSpec.JUMP, any -> true, // allow any arguments to show help message
                new AddCheckPointCommand(),
                new CreateCommand(),
                new DeleteCommand(),
                new EditCommand(),
                new HelpCommand(),
                new InfoCommand(),
                new LeaveCommand(),
                new ListCommand(),
                new ReloadCommand(),
                new RenameCommand(),
                new SaveCommand(),
                new SetDescriptionCommand(),
                new SetEndCommand(),
                new SetItemCommand(),
                new SetSpawnCommand(),
                new SetStartCommand());
    }

    @Override
    public boolean execute(JumpPlugin plugin, CommandSender sender, String[] args) {
        // TODO if player is in a jump -> leave the jump
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length > 0) {
                Text.UNKNOWN_COMMAND.send(player);
            } else if (plugin.getEditorsManager().isInEditor(player)) {
                plugin.getEditorsManager().leave(player);
            } else if (plugin.getGameManager().isPlaying(player)) {
                plugin.getGameManager().getGame(player).ifPresent(JumpGame::close);
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
