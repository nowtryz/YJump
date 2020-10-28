package fr.ycraft.jump.commands.misc;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.commands.PlayerCommand;
import fr.ycraft.jump.commands.PluginCommandExecutor;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.JumpPlayer;
import fr.ycraft.jump.inventories.JumpInventory;
import fr.ycraft.jump.inventories.ListInventory;
import fr.ycraft.jump.manager.PlayerManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class JumpsCommand extends PluginCommandExecutor implements PlayerCommand {
    private final PlayerManager playerManager;
    private final ListInventory.Factory listInventoryFactory;
    private final JumpInventory.Factory jumpInventoryFactory;

    @Inject
    public JumpsCommand(@NotNull JumpPlugin plugin,
                        PlayerManager playerManager,
                        ListInventory.Factory listInventoryFactory,
                        JumpInventory.Factory jumpInventoryFactory) {
        super(plugin, CommandSpec.JUMPS, args -> args.length <= 1);
        this.playerManager = playerManager;
        this.listInventoryFactory = listInventoryFactory;
        this.jumpInventoryFactory = jumpInventoryFactory;
    }

    @Override
    public boolean execute(JumpPlugin plugin, Player player, String[] args) {
        Optional<JumpPlayer> jumpPlayer = this.playerManager.getPlayer(player);
        if (!jumpPlayer.isPresent()) return false;

        if (args.length == 0) this.listInventoryFactory.create(jumpPlayer.get(), player).open();
        else if (args.length == 1) {
            Optional<Jump> jump = plugin.getJumpManager().getJump(args[0]);
            if (jump.isPresent()) this.jumpInventoryFactory.create(player, jumpPlayer.get(), jump.get(), null).open();
            else Text.JUMP_NOT_EXISTS.send(player);
        } else return false;
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
