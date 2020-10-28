package fr.ycraft.jump.commands;

import fr.ycraft.jump.sessions.JumpGame;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.enums.Text;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Represents a command that intends to be executed only when the command sender is on a jump
 */
public interface GameCommand extends PlayerCommand {
    @Override
    default boolean execute(JumpPlugin plugin, Player player, String[] args) {
        Optional<JumpGame> game = plugin.getGameManager().getGame(player);
        if (game.isPresent()) return execute(plugin, game.get(), player, args);
        else Text.ONLY_GAME_COMMAND.send(player);
        return true;
    }

    @Override
    default List<String> tabComplete(JumpPlugin plugin, Player player, String[] args) {
        return plugin.getGameManager()
                .getGame(player)
                .map(jumpGame -> tabComplete(plugin, jumpGame, player, args))
                .orElse(null);
    }

    boolean execute(@NotNull JumpPlugin plugin, @NotNull JumpGame game, @NotNull Player player, String[] args);
    List<String> tabComplete(@NotNull JumpPlugin plugin, @NotNull JumpGame game, @NotNull Player player, String[] argss);
}
