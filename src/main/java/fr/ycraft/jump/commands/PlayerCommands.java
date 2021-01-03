package fr.ycraft.jump.commands;

import fr.ycraft.jump.commands.enums.CommandSpec;
import fr.ycraft.jump.commands.enums.Perm;
import fr.ycraft.jump.commands.utils.GameProvider;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.manager.EditorsManager;
import fr.ycraft.jump.manager.GameManager;
import fr.ycraft.jump.sessions.JumpGame;
import net.nowtryz.mcutils.command.CommandResult;
import net.nowtryz.mcutils.command.SenderType;
import net.nowtryz.mcutils.command.annotations.Command;
import net.nowtryz.mcutils.command.annotations.ProvidesContext;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Comparator;

public class PlayerCommands {
    @Command(value = "jump", type = SenderType.PLAYER, permission = Perm.PLAY)
    public static CommandResult jump(EditorsManager editorsManager, GameManager gameManager, Player player) {
        if (editorsManager.isInEditor(player)) {
            editorsManager.leave(player);
        } else if (gameManager.isPlaying(player)) {
            gameManager.getGame(player).ifPresent(JumpGame::close);
        } else {
            sendHelp(player);
        }

        return CommandResult.SUCCESS;
    }


    @Command(value = "checkpoint", permission = Perm.PLAY)
    @ProvidesContext(value = GameProvider.class, ignoreNulls = true)
    public static CommandResult checkpoint(JumpGame game, CommandSender sender) {
        game.tpLastCheckpoint();
        return CommandResult.SUCCESS;
    }


    @Command("jump help")
    public static CommandResult help(CommandSender sender) {
        sendHelp(sender);
        return CommandResult.SUCCESS;
    }


    @ProvidesContext(value = GameProvider.class, ignoreNulls = true)
    @Command(value = "jump leave", type = SenderType.PLAYER, permission = Perm.PLAY)
    public static CommandResult leave(JumpGame game, Player player) {
        game.close();
        return CommandResult.SUCCESS;
    }


    private static void sendHelp(CommandSender target) {
        Text.HELP_HEADER.send(target);
        Arrays.stream(CommandSpec.values())
                .filter(command -> target.hasPermission(command.permission))
                .sorted(Comparator.comparing(CommandSpec::getUsage)) // slow down command but save ram
                .forEach(command -> Text.HELP_COMMAND.send(target, command.getUsage(), command.description));
    }
}
