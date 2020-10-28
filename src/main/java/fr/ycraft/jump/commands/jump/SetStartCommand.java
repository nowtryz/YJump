package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.sessions.JumpEditor;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.commands.AbstractCommandImpl;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.commands.EditorCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SetStartCommand extends AbstractCommandImpl implements EditorCommand {
    public SetStartCommand() {
        super(CommandSpec.SET_START);
    }

    @Override
    public boolean isAsync() {
        // Block change -> not async
        return false;
    }

    @Override
    public boolean execute(@NotNull JumpPlugin plugin, @NotNull JumpEditor editor, @NotNull Player player, String[] args) {
        editor.setStart(player.getLocation());
        return true;
    }

    @Override
    public List<String> tabComplete(@NotNull JumpPlugin plugin, @NotNull JumpEditor editor, @NotNull Player player, String[] args) {
        return null;
    }
}
