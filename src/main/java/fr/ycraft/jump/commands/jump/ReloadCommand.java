package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.commands.AbstractCommandImpl;
import fr.ycraft.jump.commands.CommandSpec;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

import java.util.List;

public class ReloadCommand extends AbstractCommandImpl {
    public ReloadCommand() {
        super(CommandSpec.RELOAD);
    }

    @Override
    public boolean isAsync() {
        return false; // We need to run this command in the main thread
    }

    @Override
    public boolean execute(JumpPlugin plugin, CommandSender sender, String[] args) {
        HandlerList.unregisterAll(plugin);
        plugin.onDisable();
        plugin.reloadConfig();
        plugin.onEnable();
        Text.RELOADED.send(sender);
        return true;
    }

    @Override
    public List<String> tabComplete(JumpPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
