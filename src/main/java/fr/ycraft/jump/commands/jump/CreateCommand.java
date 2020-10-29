package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.commands.AbstractCommandImpl;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.entity.Jump;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CreateCommand extends AbstractCommandImpl {
    public CreateCommand() {
        super(CommandSpec.CREATE, c -> c.length == 1);
    }

    @Override
    public boolean execute(JumpPlugin plugin, CommandSender sender, String[] args) {
        assert this.validator.test(args);
        String name = args[0];

        if (plugin.getJumpManager().getJumps().containsKey(name)) {
            Text.GAME_ALREADY_EXISTS.send(sender);
            return true;
        }

        Jump jump = plugin.getJumpManager().createAndSave(args[0]);
        if (sender instanceof Player) plugin.getEditorsManager().enter(jump, (Player) sender);
        return true;
    }

    @Override
    public List<String> tabComplete(JumpPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
