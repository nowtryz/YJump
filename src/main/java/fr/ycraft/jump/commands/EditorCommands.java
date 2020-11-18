package fr.ycraft.jump.commands;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.commands.enums.Perm;
import fr.ycraft.jump.commands.utils.EditorProvider;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.sessions.JumpEditor;
import net.nowtryz.mcutils.command.CommandResult;
import net.nowtryz.mcutils.command.SenderType;
import net.nowtryz.mcutils.command.annotations.Arg;
import net.nowtryz.mcutils.command.annotations.Command;
import net.nowtryz.mcutils.command.annotations.ProvidesContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EditorCommands {
    @ProvidesContext(EditorProvider.class)
    @Command(value = "jump addcheckpoint", type = SenderType.PLAYER, permission = Perm.EDIT)
    public static CommandResult addCheckpoint(JumpEditor editor, Player player) {
        if (editor == null) {
            Text.EDITOR_ONLY_COMMAND.send(player);
            return CommandResult.FAILED;
        }

        editor.addCheckpoint(player.getLocation());
        return CommandResult.SUCCESS;
    }


    @ProvidesContext(EditorProvider.class)
    @Command(value = "jump rename <name>", type = SenderType.PLAYER, permission = Perm.EDIT)
    public static CommandResult rename(JumpEditor editor, Player player, @Arg("name") String name) {
        if (editor == null) {
            Text.EDITOR_ONLY_COMMAND.send(player);
            return CommandResult.FAILED;
        }

        editor.getJump().setName(name);
        Text.NAME_UPDATED.send(player, name);
        return CommandResult.SUCCESS;
    }


    @ProvidesContext(EditorProvider.class)
    @Command(value = "jump save", type = SenderType.PLAYER, permission = Perm.EDIT)
    public static CommandResult save(JumpPlugin plugin, JumpEditor editor, Player player) {
        if (editor == null) {
            Text.EDITOR_ONLY_COMMAND.send(player);
            return CommandResult.FAILED;
        }

        plugin.getEditorsManager().leave(player);
        return CommandResult.SUCCESS;
    }


    // TODO parse and get all remaining args
    @ProvidesContext(EditorProvider.class)
    @Command(value = "jump setdesc <description...>", type = SenderType.PLAYER, permission = Perm.EDIT)
    public static CommandResult setDescription(JumpEditor editor, Player player, @Arg("description") String description) {
        if (editor == null) {
            Text.EDITOR_ONLY_COMMAND.send(player);
            return CommandResult.FAILED;
        }

        editor.getJump().setDescription(description);
        Text.DESCRIPTION_UPDATED.send(player);
        return CommandResult.SUCCESS;
    }


    @ProvidesContext(EditorProvider.class)
    @Command(value = "jump setend", type = SenderType.PLAYER, permission = Perm.EDIT)
    public static CommandResult setEnd(JumpEditor editor, Player player) {
        if (editor == null) {
            Text.EDITOR_ONLY_COMMAND.send(player);
            return CommandResult.FAILED;
        }

        editor.setSEnd(player.getLocation());
        return CommandResult.SUCCESS;
    }


    @ProvidesContext(EditorProvider.class)
    @Command(value = "jump setitem", type = SenderType.PLAYER, permission = Perm.EDIT)
    public static CommandResult setItem(JumpEditor editor, Player player) {
        if (editor == null) {
            Text.EDITOR_ONLY_COMMAND.send(player);
            return CommandResult.FAILED;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().equals(Material.AIR)) {
            Text.ITEM_AIR.send(player);
        } else {
            editor.getJump().setItem(item);
            Text.ITEM_UPDATED.send(player, item.getType().toString());
        }

        return CommandResult.SUCCESS;
    }


    @ProvidesContext(EditorProvider.class)
    @Command(value = "jump setspawn", type = SenderType.PLAYER, permission = Perm.EDIT)
    public static CommandResult setSpawn(JumpEditor editor, Player player) {
        if (editor == null) {
            Text.EDITOR_ONLY_COMMAND.send(player);
            return CommandResult.FAILED;
        }
        editor.setSpawn(player.getLocation());
        return CommandResult.SUCCESS;
    }


    @ProvidesContext(EditorProvider.class)
    @Command(value = "jump setstart", type = SenderType.PLAYER, permission = Perm.EDIT)
    public static CommandResult setStart(JumpEditor editor, Player player) {
        if (editor == null) {
            Text.EDITOR_ONLY_COMMAND.send(player);
            return CommandResult.FAILED;
        }
        editor.setStart(player.getLocation());
        return CommandResult.SUCCESS;
    }
}
