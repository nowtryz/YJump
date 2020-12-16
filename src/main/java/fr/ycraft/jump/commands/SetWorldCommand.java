package fr.ycraft.jump.commands;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.commands.enums.Perm;
import fr.ycraft.jump.commands.utils.JumpCompleter;
import fr.ycraft.jump.commands.utils.JumpProvider;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.manager.JumpManager;
import lombok.RequiredArgsConstructor;
import net.nowtryz.mcutils.command.CommandResult;
import net.nowtryz.mcutils.command.annotations.Arg;
import net.nowtryz.mcutils.command.annotations.Command;
import net.nowtryz.mcutils.command.annotations.Completer;
import net.nowtryz.mcutils.command.annotations.ProvidesArg;
import net.nowtryz.mcutils.command.contexts.CompletionContext;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor_={@Inject})
public class SetWorldCommand {
    private final JumpPlugin plugin;
    private final JumpManager manager;
    private final JumpCompleter completer;

    @ProvidesArg(target = "jump", provider = JumpProvider.class)
    @Command(value = "jump setworld <jump> <world>", permission = Perm.EDIT)
    public CommandResult execute(@Arg("jump") Jump jump, @Arg("world") String worldName, CommandSender sender) {
        if (jump == null) {
            Text.JUMP_NOT_EXISTS.send(sender);
            return CommandResult.FAILED;
        }

        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            Text.UNKNOWN_WORLD.send(sender, worldName, Bukkit.getWorlds()
                    .parallelStream()
                    .map(World::getName)
                    .collect(Collectors.joining(", ")));
            return CommandResult.FAILED;
        } else {
            jump.setWorld(world);
            Text.SUCCESS_WORLD_SET.send(sender, jump.getName(), world.getName());
            this.manager.updateJumpList();
            this.manager.replacePlates();
            return CommandResult.SUCCESS;
        }

    }

    @Completer(value = "jump setworld <jump>")
    public List<String> completeJump(CompletionContext context) {
        return this.completer.tabComplete(context);
    }

    @Completer(value = "jump setworld <jump> <world>")
    public List<String> completeWorld(CompletionContext context) {
        return Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(s -> s.startsWith(context.getArgument()))
                .collect(Collectors.toList());
    }
}
