package fr.ycraft.jump.listeners;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.manager.EditorsManager;
import fr.ycraft.jump.manager.GameManager;
import fr.ycraft.jump.manager.JumpManager;
import net.nowtryz.mcutils.LocationUtil;
import net.nowtryz.mcutils.listener.AbstractListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
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

        if (!this.gameManager.isPlaying(event.getPlayer())) {
            Jump jump = this.jumpManager
                    .getJumpStarts()
                    .get(LocationUtil.toBlock(event.getClickedBlock().getLocation()));

            if (jump != null) {
                if (!this.editorsManager.isInEditor(event.getPlayer())) {
                    this.gameManager.enter(event.getPlayer(), jump);
                } else {
                    Text.EDITOR_NO_GAME.send(event.getPlayer());
                }
            }
        }
    }
}
