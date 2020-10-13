package fr.ycraft.jump.listeners;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.manager.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PlayerListener extends AbstractListener {
    private final PlayerManager playerManager;

    @Inject
    public PlayerListener(JumpPlugin plugin, PlayerManager playerManager) {
        super(plugin);
        this.playerManager = playerManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.playerManager.load(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.playerManager.unload(event.getPlayer());
    }
}
