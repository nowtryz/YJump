package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.commands.AbstractCommandImpl;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.commands.PlayerCommand;
import fr.ycraft.jump.entity.Jump;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EditCommand extends AbstractCommandImpl implements PlayerCommand {
    public EditCommand() {
        super(CommandSpec.EDIT, c -> c.length == 1);
    }

    @Override
    public boolean execute(JumpPlugin plugin, Player player, String[] args) {
        assert this.validator.test(args);
        String name = args[0];
        Optional<Jump> jump = plugin.getJumpManager().getJump(name);

        if (jump.isPresent()) {
            plugin.getEditorsManager().enter(jump.get(), player);
            return true;
        } else {
            Text.JUMP_NOT_EXISTS.send(player);
        }

        return true;
    }

    @Override
    public List<String> tabComplete(JumpPlugin plugin, Player player, String[] args) {
        if (args.length == 0) return null;
        return plugin.getJumpManager()
                .getJumps()
                .values()
                .stream()
                .map(Jump::getName)
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }
}
