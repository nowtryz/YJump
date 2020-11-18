package fr.ycraft.jump.commands;

import fr.ycraft.jump.commands.enums.Perm;
import fr.ycraft.jump.commands.utils.EditorProvider;
import fr.ycraft.jump.commands.utils.JumpProvider;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.Position;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.inventories.InfoAdminInventory;
import fr.ycraft.jump.sessions.JumpEditor;
import lombok.RequiredArgsConstructor;
import net.nowtryz.mcutils.command.CommandResult;
import net.nowtryz.mcutils.command.SenderType;
import net.nowtryz.mcutils.command.annotations.Arg;
import net.nowtryz.mcutils.command.annotations.Command;
import net.nowtryz.mcutils.command.annotations.ProvidesArg;
import net.nowtryz.mcutils.command.annotations.ProvidesContext;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor_={@Inject})
public class InfoCommand {
    private final InfoAdminInventory.Factory factory;

    @ProvidesContext(EditorProvider.class)
    @Command(value = "jump info", type = SenderType.PLAYER, permission = Perm.ADMIN_LIST)
    public CommandResult execute(JumpEditor editor, Player player) {
        if (editor == null) {
            Text.EDITOR_ONLY_COMMAND.send(player);
            return CommandResult.FAILED;
        }

        this.factory.create(player, editor.getJump(), null).open();
        return CommandResult.SUCCESS;
    }

    @ProvidesArg(target = "jump", provider = JumpProvider.class)
    @Command(value = "jump info <jump>", permission = Perm.ADMIN_LIST)
    public CommandResult execute(@Arg("jump") Jump jump, CommandSender sender) {
        if (jump == null) {
            Text.JUMP_NOT_EXISTS.send(sender);
            return CommandResult.FAILED;
        }

        if (sender instanceof Player) {
            this.factory.create((Player) sender, jump, null).open();
        } else {
            Text.JUMP_INFO.send(sender,
                    jump.getName(),
                    jump.getFallDistance(),
                    jump.getDescription().orElse(""),
                    Optional.ofNullable(jump.getWorld()).map(World::getName).orElse(null),
                    jump.getItem().getType(),
                    jump.getCheckpointCount(),
                    jump.getSpawnPos().map(Position::getX).orElse(0),
                    jump.getSpawnPos().map(Position::getY).orElse(0),
                    jump.getSpawnPos().map(Position::getY).orElse(0),
                    jump.getStartPos().map(Position::getX).orElse(0),
                    jump.getStartPos().map(Position::getY).orElse(0),
                    jump.getStartPos().map(Position::getY).orElse(0),
                    jump.getEndPos().map(Position::getX).orElse(0),
                    jump.getEndPos().map(Position::getY).orElse(0),
                    jump.getEndPos().map(Position::getY).orElse(0)
            );
        }
        return CommandResult.SUCCESS;
    }
}
