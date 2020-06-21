package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.JumpGame;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.commands.AbstractCommandImpl;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.commands.GameCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LeaveCommand extends AbstractCommandImpl implements GameCommand {
    public LeaveCommand() {
        super(CommandSpec.LEAVE);
    }

    @Override
    public boolean execute(@NotNull JumpPlugin plugin, @NotNull JumpGame game, @NotNull Player player, String[] args) {
        game.close();
        return true;
    }

    @Override
    public List<String> tabComplete(@NotNull JumpPlugin plugin, @NotNull JumpGame game, @NotNull Player player, String[] argss) {
        return null;
    }
}
