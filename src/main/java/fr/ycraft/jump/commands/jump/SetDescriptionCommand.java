package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.sessions.JumpEditor;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.commands.AbstractCommandImpl;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.commands.EditorCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SetDescriptionCommand extends AbstractCommandImpl implements EditorCommand {
    public SetDescriptionCommand() {
        super(CommandSpec.SET_DESCRIPTION, args -> args.length > 0);
    }

    @Override
    public boolean execute(@NotNull JumpPlugin plugin, @NotNull JumpEditor editor, @NotNull Player player, String[] args) {
        editor.getJump().setDescription(String.join(" ", args));
        Text.DESCRIPTION_UPDATED.send(player);
        return true;
    }

    @Override
    public List<String> tabComplete(@NotNull JumpPlugin plugin, @NotNull JumpEditor editor, @NotNull Player player, String[] args) {
        return null;
    }
}
