package fr.ycraft.jump.commands;

import fr.ycraft.jump.commands.enums.Perm;
import fr.ycraft.jump.commands.utils.JumpCompleter;
import fr.ycraft.jump.commands.utils.JumpProvider;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.manager.EditorsManager;
import lombok.RequiredArgsConstructor;
import net.nowtryz.mcutils.command.CommandResult;
import net.nowtryz.mcutils.command.SenderType;
import net.nowtryz.mcutils.command.annotations.Arg;
import net.nowtryz.mcutils.command.annotations.Command;
import net.nowtryz.mcutils.command.annotations.Completer;
import net.nowtryz.mcutils.command.annotations.ProvidesArg;
import net.nowtryz.mcutils.command.contexts.CompletionContext;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.util.List;

@RequiredArgsConstructor(onConstructor_={@Inject})
public class EditCommand {
    private final JumpCompleter completer;

    @ProvidesArg(target = "<jump>", provider = JumpProvider.class, ignoreNulls = true)
    @Command(value = "jump edit <jump>", type = SenderType.PLAYER, permission = Perm.EDIT)
    public CommandResult execute(EditorsManager manager, Player player, @Arg("jump") Jump jump) {
        manager.enter(jump, player);
        return CommandResult.SUCCESS;
    }

    @Completer(value = "jump edit <jump>", type = SenderType.PLAYER)
    public List<String> tabComplete(CompletionContext context) {
        return this.completer.tabComplete(context);
    }
}
