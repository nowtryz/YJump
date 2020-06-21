package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.JumpEditor;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.commands.AbstractCommandImpl;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.commands.EditorCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SetEndCommand extends AbstractCommandImpl implements EditorCommand {
    public SetEndCommand() {
        super(CommandSpec.SET_END);
    }

    @Override
    public boolean isAsync() {
        // Block change -> not async
        return false;
    }

    @Override
    public boolean execute(@NotNull JumpPlugin plugin, @NotNull JumpEditor editor, @NotNull Player player, String[] args) {
        editor.setSEnd(player.getLocation());
        return true;
    }

    @Override
    public List<String> tabComplete(@NotNull JumpPlugin plugin, @NotNull JumpEditor editor, @NotNull Player player, String[] args) {
        return null;
    }
}
