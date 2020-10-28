package fr.ycraft.jump.listeners;

import com.google.inject.assistedinject.Assisted;
import fr.ycraft.jump.sessions.JumpGame;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.manager.GameManager;
import net.nowtryz.mcutils.listener.AbstractListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

import javax.inject.Inject;

public class GameListener extends AbstractListener {
    private final GameManager gameManager;
    private final Config config;

    public interface Factory {
        GameListener create(GameManager gameManager);
    }

    @Inject
    public GameListener(JumpPlugin plugin, Config config, @Assisted GameManager gameManager) {
        super(plugin);
        this.config = config;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        this.gameManager
                .getGame(event.getPlayer())
                .ifPresent(game -> Bukkit.getScheduler().runTask(this.plugin, game::close));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        this.gameManager.getGame(event.getPlayer()).ifPresent(game -> game.onCommand(event));
    }

    @EventHandler
    public void onFly(PlayerToggleFlightEvent event) {
        this.gameManager.getGame(event.getPlayer()).ifPresent(game -> game.onFly(event));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        this.gameManager.getGame(event.getPlayer()).ifPresent(game -> game.onInteract(event));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (this.config.get(Key.MAX_FALL_DISTANCE) < 0) return;
        this.gameManager.getGame(event.getPlayer()).ifPresent(JumpGame::onMove);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            this.gameManager.getGame((Player) event.getEntity()).ifPresent(game -> game.onDamage(event));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            this.gameManager.getGame((Player) event.getEntity()).ifPresent(game -> event.setCancelled(true));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        this.gameManager.getGame(event.getPlayer()).ifPresent(game -> event.setCancelled(true));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        this.gameManager.getGame(event.getPlayer()).ifPresent(game -> game.onTeleport(event));
    }

    // NOTE: EntityPotionEffectEvent could be used as of 1.13
}
