package fr.ycraft.jump.commands;

import fr.ycraft.jump.commands.enums.Perm;
import fr.ycraft.jump.commands.utils.JumpCompleter;
import fr.ycraft.jump.commands.utils.JumpProvider;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.JumpPlayer;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.inventories.JumpInventory;
import fr.ycraft.jump.inventories.ListInventory;
import fr.ycraft.jump.manager.PlayerManager;
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
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class JumpsCommand {
    private final PlayerManager playerManager;
    private final JumpCompleter completer;

    @Command(value = "jumps", type = SenderType.PLAYER, permission = Perm.LIST)
    public CommandResult execute(ListInventory.Factory listInventoryFactory, Player player) {
        Optional<JumpPlayer> jumpPlayer = this.playerManager.getPlayer(player);
        // All connected users should be in cache
        if (!jumpPlayer.isPresent()) return CommandResult.INTERNAL_ERROR;

        listInventoryFactory.create(jumpPlayer.get(), player).open();
        return CommandResult.SUCCESS;
    }

    @Command(value = "jumps <jump>", type = SenderType.PLAYER, permission = Perm.PLAY)
    @ProvidesArg(target = "jump", provider = JumpProvider.class)
    public CommandResult execute(JumpInventory.Factory jumpInventoryFactory, Player player, @Arg("jump") Jump jump) {
        Optional<JumpPlayer> jumpPlayer = this.playerManager.getPlayer(player);
        // All connected users should be in cache
        if (!jumpPlayer.isPresent()) return CommandResult.INTERNAL_ERROR;

        if (jump == null) {
            Text.JUMP_NOT_EXISTS.send(player);
            return CommandResult.FAILED;
        }

        jumpInventoryFactory.create(player, jumpPlayer.get(), jump, null).open();
        return CommandResult.SUCCESS;
    }

    @Completer(value = "jumps <jump>", type = SenderType.PLAYER)
    public List<String> tabComplete(CompletionContext context) {
        return this.completer.tabComplete(context);
    }
}
