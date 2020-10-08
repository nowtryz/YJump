package fr.ycraft.jump.listeners;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.manager.EditorsManager;
import fr.ycraft.jump.manager.GameManager;
import fr.ycraft.jump.manager.JumpManager;
import fr.ycraft.jump.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.inject.Inject;
import java.util.Map;

public class PlateListener extends AbstractListener {
    private @Inject EditorsManager editorsManager;
    private @Inject GameManager gameManager;
    private @Inject JumpManager jumpManager;

    @Inject
    public PlateListener(JumpPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (
                !event.getAction().equals(Action.PHYSICAL)
                        || event.getClickedBlock() == null
                        || !Jump.ALLOWED_MATERIALS.contains(event.getClickedBlock().getType())
        ) return;

        Location loc = event.getClickedBlock().getLocation();

        if (!this.gameManager.isPlaying(event.getPlayer())) {
            Map<Location, Jump> jumpStarts = this.jumpManager.getJumpStarts();
            for (Map.Entry<Location, Jump> entry: jumpStarts.entrySet()) {
                if (LocationUtil.isBlockLocationEqual(entry.getKey(), loc)) {
                    if (!this.editorsManager.isInEditor(event.getPlayer())) {
                        this.gameManager.enter(event.getPlayer(), entry.getValue());
                    } else {
                        Text.EDITOR_NO_GAME.send(event.getPlayer());
                    }
                    break;
                }
            }
        }
    }
}
