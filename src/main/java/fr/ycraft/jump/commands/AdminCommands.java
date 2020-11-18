package fr.ycraft.jump.commands;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.commands.enums.Perm;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.manager.JumpManager;
import net.nowtryz.mcutils.command.CommandResult;
import net.nowtryz.mcutils.command.annotations.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import java.util.Set;

public class AdminCommands {
    @Command(value = "jump list", permission = Perm.ADMIN_LIST)
    public static CommandResult list(JumpManager manager, CommandSender sender) {
        Set<String> jumps = manager.getJumps().keySet();
        sender.sendMessage(Text.JUMP_LIST.get(String.join(", ", jumps)));
        return CommandResult.SUCCESS;
    }

    @Command(value = "jump reload", permission = Perm.RELOAD)
    public static CommandResult reload(JumpPlugin plugin, CommandSender sender) {
        HandlerList.unregisterAll(plugin);
        plugin.onDisable();
        plugin.reloadConfig();
        plugin.onEnable();
        Text.RELOADED.send(sender);
        return CommandResult.SUCCESS;
    }
}
