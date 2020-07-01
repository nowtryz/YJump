package fr.ycraft.jump.listeners;

import fr.ycraft.jump.JumpGame;
import fr.ycraft.jump.JumpPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

public class GameListener extends AbstractListener {
    public GameListener(JumpPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        this.plugin.getGameManager().getGame(event.getPlayer()).ifPresent(JumpGame::close);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        this.plugin.getGameManager().getGame(event.getPlayer()).ifPresent(game -> game.onCommand(event));
    }

    @EventHandler
    public void onFly(PlayerToggleFlightEvent event) {
        this.plugin.getGameManager().getGame(event.getPlayer()).ifPresent(game -> game.onFly(event));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
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

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            this.plugin.getGameManager().getGame((Player) event.getEntity()).ifPresent(game -> event.setCancelled(true));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPotion(PlayerItemConsumeEvent event) {
        this.plugin.getGameManager().getGame(event.getPlayer()).ifPresent(game -> event.setCancelled(true));
    }
}
