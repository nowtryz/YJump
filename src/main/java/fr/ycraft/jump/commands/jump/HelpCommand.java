package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.commands.AbstractCommandImpl;
import fr.ycraft.jump.commands.CommandSpec;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class HelpCommand extends AbstractCommandImpl {
    public HelpCommand() {
        super(CommandSpec.HELP);
    }

    @Override
    public boolean execute(JumpPlugin plugin, CommandSender sender, String[] args) {
        HelpCommand.sendHelp(sender);
        return true;
    }

    @Override
    public List<String> tabComplete(JumpPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }

    static void sendHelp(CommandSender target) {
        Text.HELP_HEADER.send(target);
        Arrays.stream(CommandSpec.values())
                .filter(command -> command.permission.isHeldBy(target))
                .forEach(command -> Text.HELP_COMMAND.send(target, command.getUsage(), command.description));
    }
}
