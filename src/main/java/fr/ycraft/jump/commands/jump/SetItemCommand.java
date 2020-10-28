package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.sessions.JumpEditor;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.commands.AbstractCommandImpl;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.commands.EditorCommand;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SetItemCommand extends AbstractCommandImpl implements EditorCommand {
    public SetItemCommand() {
        super(CommandSpec.SET_ITEM);
    }

    @Override
    public boolean execute(@NotNull JumpPlugin plugin, @NotNull JumpEditor editor, @NotNull Player player, String[] args) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().equals(Material.AIR)) {
           Text.ITEM_AIR.send(player);
        } else {
            editor.getJump().setItem(item);
            Text.ITEM_UPDATED.send(player, item.getType().toString());
        }

        return true;
    }

    @Override
    public List<String> tabComplete(@NotNull JumpPlugin plugin, @NotNull JumpEditor editor, @NotNull Player player, String[] args) {
        return null;
    }
}
