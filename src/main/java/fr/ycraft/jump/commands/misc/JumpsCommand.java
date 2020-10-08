package fr.ycraft.jump.commands.misc;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JumpsCommand extends PluginCommandExecutor implements PlayerCommand {
    private final PlayerManager playerManager;

    @Inject
    public JumpsCommand(@NotNull JumpPlugin plugin, PlayerManager playerManager) {
        super(plugin, CommandSpec.JUMPS, args -> args.length <= 1);
        this.playerManager = playerManager;
    }

    @Override
    public boolean execute(JumpPlugin plugin, Player player, String[] args) {
        Optional<JumpPlayer> jumpPlayer = this.playerManager.getPlayer(player);
        if (!jumpPlayer.isPresent()) return false;

        if (args.length == 0) new ListInventory(this.plugin, jumpPlayer.get(), player);
        else if (args.length == 1) {
            Optional<Jump> jump = plugin.getJumpManager().getJump(args[0]);
            if (jump.isPresent()) new JumpInventory(plugin, player, jumpPlayer.get(), jump.get());
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
