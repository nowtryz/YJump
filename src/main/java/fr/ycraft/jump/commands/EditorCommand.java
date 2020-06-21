package fr.ycraft.jump.commands;

import fr.ycraft.jump.JumpEditor;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Represents a command that intends to be executed only when the command sender is in an editor
 */
public interface EditorCommand extends PlayerCommand {
    @Override
    default boolean execute(JumpPlugin plugin, Player player, String[] args) {
        Optional<JumpEditor> editor = plugin.getEditorsManager().getEditor(player);
        if (editor.isPresent()) return execute(plugin, editor.get(), player, args);
        else Text.EDITOR_ONLY_COMMAND.send(player);
        return true;
    }

    @Override
    default List<String> tabComplete(JumpPlugin plugin, Player player, String[] args) {
        return plugin.getEditorsManager()
                .getEditor(player)
                .map(jumpEditor -> tabComplete(plugin, jumpEditor, player, args))
                .orElse(null);
    }

    boolean execute(@NotNull JumpPlugin plugin, @NotNull JumpEditor editor, @NotNull Player player, String[] args);
    List<String> tabComplete(@NotNull JumpPlugin plugin, @NotNull JumpEditor editor, @NotNull Player player, String[] args);
}
