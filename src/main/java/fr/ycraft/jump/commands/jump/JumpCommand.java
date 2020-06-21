package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.JumpGame;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.commands.CommandImpl;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.commands.PluginCommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JumpCommand extends PluginCommandExecutor {
    public JumpCommand(@NotNull JumpPlugin plugin) {
        super(plugin, CommandSpec.JUMP,
                new CreateCommand(),
                new EditCommand(),
                new SaveCommand(),
                new LeaveCommand(),
                new ListCommand(),
                new SetSpawnCommand(),
                new SetStartCommand(),
                new SetEndCommand(),
                new ReloadCommand(),
                new DeleteCommand(),
                new AddCheckPointCommand(),
                new HelpCommand());
    }

    @Override
    public boolean execute(JumpPlugin plugin, CommandSender sender, String[] args) {
        // TODO if player is in a jump -> leave the jump
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (plugin.getEditorsManager().isInEditor(player)) {
                plugin.getEditorsManager().leave(player);
            } else if (plugin.getGameManager().isPlaying(player)) {
                plugin.getGameManager().getGame(player).ifPresent(JumpGame::close);
            } else {
                Text.UNKNOWN_COMMAND.send(sender);
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
