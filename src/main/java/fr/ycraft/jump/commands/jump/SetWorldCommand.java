package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.commands.AbstractCommandImpl;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.manager.JumpManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SetWorldCommand extends AbstractCommandImpl {
    private final JumpManager jumpManager;

    @Inject
    public SetWorldCommand(JumpManager jumpManager) {
        super(CommandSpec.SET_WORLD,c -> c.length >= 1);
        this.jumpManager = jumpManager;
    }

    @Override
    public boolean isAsync() {
        return false; // world check
    }

    @Override
    public boolean execute(JumpPlugin plugin, CommandSender sender, String[] args) {
        assert this.validator.test(args);
        String name = args[0];
        Optional<Jump> optionalJump = plugin.getJumpManager().getJump(name);

        if (!optionalJump.isPresent()) Text.JUMP_NOT_EXISTS.send(sender);
        else {
            Jump jump = optionalJump.get();
            World world = Bukkit.getWorld(args[1]);

            if (world == null) Text.UNKNOWN_WORLD.send(sender, args[1], Bukkit.getWorlds()
                    .parallelStream()
                    .map(World::getName)
                    .collect(Collectors.joining(", ")));
            else {
                jump.setWorld(world);
                Text.SUCCESS_WORLD_SET.send(sender, jump.getName(), world.getName());
                this.jumpManager.updateJumpList();
                plugin.replacePlates();
            }
        }

        return true;
    }

    @Override
    public List<String> tabComplete(JumpPlugin plugin, CommandSender sender, String[] args) {
        switch (args.length) {
            case 1: return plugin.getJumpManager()
                    .getJumps()
                    .values()
                    .stream()
                    .map(Jump::getName)
                    .map(String::toLowerCase)
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            case 2: return Bukkit.getWorlds()
                    .stream()
                    .map(World::getName)
                    .map(String::toLowerCase)
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            default: return null;
        }
    }
}
