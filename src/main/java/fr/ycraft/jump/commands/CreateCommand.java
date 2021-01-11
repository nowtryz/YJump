package fr.ycraft.jump.commands;

import fr.ycraft.jump.commands.enums.Perm;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.manager.EditorsManager;
import fr.ycraft.jump.manager.JumpManager;
import lombok.RequiredArgsConstructor;
import net.nowtryz.mcutils.command.CommandResult;
import net.nowtryz.mcutils.command.annotations.Arg;
import net.nowtryz.mcutils.command.annotations.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor_={@Inject})
public class CreateCommand {
    private final JumpManager jumpManager;
    private final EditorsManager editorsManager;

    @Command(value = "jump create <name>", permission = Perm.CREATE)
    public CommandResult execute(CommandSender sender, @Arg("name") String name) {
        if (this.jumpManager.getJumps().containsKey(name)) {
            Text.GAME_ALREADY_EXISTS.send(sender);
            return CommandResult.FAILED;
        }

        if (!Jump.isCorrectName(name)) {
            Text.NAME_TOO_LONG.send(sender);
            return CommandResult.FAILED;
        }

        Jump jump = this.jumpManager.createAndSave(name);
        if (sender instanceof Player) this.editorsManager.enter(jump, (Player) sender);
        return CommandResult.SUCCESS;
    }
}
