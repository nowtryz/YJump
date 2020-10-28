package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.commands.AbstractCommandImpl;
import fr.ycraft.jump.commands.CommandSpec;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;

public class ListCommand extends AbstractCommandImpl {
    public ListCommand() {
        super(CommandSpec.LIST);
    }

    @Override
    public boolean execute(JumpPlugin plugin, CommandSender sender, String[] args) {
        Set<String> jumps = plugin.getJumpManager().getJumps().keySet();
        sender.sendMessage(Text.JUMP_LIST.get(String.join(", ", jumps)));
        return true;
    }

    @Override
    public List<String> tabComplete(JumpPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
