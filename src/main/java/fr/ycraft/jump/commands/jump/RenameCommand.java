package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.JumpEditor;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.commands.AbstractCommandImpl;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.commands.EditorCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RenameCommand extends AbstractCommandImpl implements EditorCommand {
    public RenameCommand() {
        super(CommandSpec.RENAME, args -> args.length == 1);
    }

    @Override
    public boolean execute(@NotNull JumpPlugin plugin, @NotNull JumpEditor editor, @NotNull Player player, String[] args) {
        plugin.getJumpManager().updateName(editor.getJump(), args[0]);
        Text.NAME_UPDATED.send(player, args[0]);
        return true;
    }

    @Override
    public List<String> tabComplete(@NotNull JumpPlugin plugin, @NotNull JumpEditor editor, @NotNull Player player, String[] args) {
        return null;
    }
}
