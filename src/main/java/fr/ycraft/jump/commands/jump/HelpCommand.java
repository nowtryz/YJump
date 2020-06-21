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
        Text.HELP_HEADER.send(sender);
        Arrays.stream(CommandSpec.values())
                .filter(command -> command.permission.isHeldBy(sender))
                .forEach(command -> Text.HELP_COMMAND.send(sender, command.getUsage(), command.description));

        return true;
    }

    @Override
    public List<String> tabComplete(JumpPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
