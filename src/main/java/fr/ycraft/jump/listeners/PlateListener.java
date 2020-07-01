package fr.ycraft.jump.listeners;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;

public class PlateListener implements Listener {
    private final JumpPlugin plugin;

    public PlateListener(JumpPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (
                !event.getAction().equals(Action.PHYSICAL)
                        || event.getClickedBlock() == null
                        || !Jump.ALLOWED_MATERIALS.contains(event.getClickedBlock().getType())
        ) return;

        Location loc = event.getClickedBlock().getLocation();

        if (!this.plugin.getGameManager().isPlaying(event.getPlayer())) {
            Map<Location, Jump> jumpStarts = this.plugin.getJumpManager().getJumpStarts();
            for (Map.Entry<Location, Jump> entry: jumpStarts.entrySet()) {
                if (LocationUtil.isBlockLocationEqual(entry.getKey(), loc)) {
                    if (!this.plugin.getEditorsManager().isInEditor(event.getPlayer())) {
                        this.plugin.getGameManager().enter(event.getPlayer(), entry.getValue());
                    } else {
                        Text.EDITOR_NO_GAME.send(event.getPlayer());
                    }
                    break;
                }
            }
        }
    }
}
