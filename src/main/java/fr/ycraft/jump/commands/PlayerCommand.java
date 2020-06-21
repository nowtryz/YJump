package fr.ycraft.jump.commands;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Represents a commend that intends to only be executed by a player
 */
public interface PlayerCommand extends CommandImpl {
    @Override
    default boolean isPlayerCommand() {
        return true;
    }

    @Override
    default boolean execute(JumpPlugin plugin, CommandSender sender, String[] args) {
        if (this.isPlayerCommand() && !(sender instanceof Player)) Text.ONLY_PLAYER_COMMAND.send(sender);
        else return this.execute(plugin, (Player) sender, args);
        return true;
    }

    @Override
    default List<String> tabComplete(JumpPlugin plugin, CommandSender sender, String[] args) {
        if (sender instanceof Player) return tabComplete(plugin, (Player) sender, args);
        return null;
    }

    boolean execute(JumpPlugin plugin, Player player, String[] args);
    List<String> tabComplete(JumpPlugin plugin, Player player, String[] args);
}
