package fr.ycraft.jump.listeners;

import fr.ycraft.jump.JumpGame;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

public class GameListener extends AbstractListener {
    public GameListener(JumpPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        this.plugin.getGameManager().getGame(event.getPlayer()).ifPresent(JumpGame::close);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        this.plugin.getGameManager().getGame(event.getPlayer()).ifPresent(game -> game.onCommand(event));
    }

    @EventHandler
    public void onFly(PlayerToggleFlightEvent event) {
        this.plugin.getGameManager().getGame(event.getPlayer()).ifPresent(game -> game.onFly(event));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.PHYSICAL)
                || event.getClickedBlock() == null
                || !Jump.ALLOWED_MATERIALS.contains(event.getClickedBlock().getType())
        ) return;

        this.plugin.getGameManager().getGame(event.getPlayer()).ifPresent(game -> game.onInteract(event));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (this.plugin.getConfigProvider().getMaxFallDistance() < 0) return;
        this.plugin.getGameManager().getGame(event.getPlayer()).ifPresent(JumpGame::onMove);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            this.plugin.getGameManager().getGame((Player) event.getEntity()).ifPresent(game -> game.onDamage(event));
        }
    }
}
