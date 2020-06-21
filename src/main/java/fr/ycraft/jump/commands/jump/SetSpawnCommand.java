package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.JumpEditor;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.commands.AbstractCommandImpl;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.commands.EditorCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SetSpawnCommand extends AbstractCommandImpl implements EditorCommand {
    public SetSpawnCommand() {
        super(CommandSpec.SET_SPAWN);
    }

    @Override
    public boolean isAsync() {
        // Block change -> not async
        return false;
    }

    @Override
    public boolean execute(@NotNull JumpPlugin plugin, @NotNull JumpEditor editor, @NotNull Player player, String[] args) {
        editor.setSpawn(player.getLocation());
        return true;
    }

    @Override
    public List<String> tabComplete(@NotNull JumpPlugin plugin, @NotNull JumpEditor editor, @NotNull Player player, String[] args) {
        return null;
    }
}
