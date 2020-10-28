package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.sessions.JumpEditor;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.commands.AbstractCommandImpl;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.commands.EditorCommand;
import fr.ycraft.jump.inventories.InfoAdminInventory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.List;

public class InfoCommand extends AbstractCommandImpl implements EditorCommand {
    private final InfoAdminInventory.Factory factory;

    @Inject
    InfoCommand(InfoAdminInventory.Factory factory) {
        super(CommandSpec.INFO);
        this.factory = factory;
    }

    @Override
    public boolean execute(@NotNull JumpPlugin plugin, @NotNull JumpEditor editor, @NotNull Player player, String[] args) {
        this.factory.create(player, editor.getJump(), null).open();
        return true;
    }

    @Override
    public List<String> tabComplete(@NotNull JumpPlugin plugin, @NotNull JumpEditor editor, @NotNull Player player, String[] args) {
        return null;
    }
}
