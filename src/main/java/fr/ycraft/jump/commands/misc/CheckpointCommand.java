package fr.ycraft.jump.commands.misc;

import fr.ycraft.jump.sessions.JumpGame;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.commands.GameCommand;
import fr.ycraft.jump.commands.PluginCommandExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class CheckpointCommand extends PluginCommandExecutor implements GameCommand {
    @Inject
    public CheckpointCommand(@NotNull JumpPlugin plugin) {
        super(plugin, CommandSpec.CHECKPOINT);
    }

    @Override
    public boolean execute(@NotNull JumpPlugin plugin, @NotNull JumpGame game, @NotNull Player player, String[] args) {
        game.tpLastCheckpoint();
        return true;
    }

    @Override
    public List<String> tabComplete(@NotNull JumpPlugin plugin, @NotNull JumpGame game, @NotNull Player player, String[] argss) {
        return null;
    }
}
