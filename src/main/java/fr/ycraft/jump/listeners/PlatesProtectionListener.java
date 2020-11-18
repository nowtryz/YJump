package fr.ycraft.jump.listeners;

import fr.ycraft.jump.sessions.JumpEditor;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.commands.enums.Perm;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.manager.EditorsManager;
import fr.ycraft.jump.manager.JumpManager;
import net.nowtryz.mcutils.LocationUtil;
import net.nowtryz.mcutils.listener.AbstractListener;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class PlatesProtectionListener extends AbstractListener {
    private @Inject EditorsManager editorsManager;
    private @Inject JumpManager jumpManager;
    private @Inject Config config;

    @Inject
    public PlatesProtectionListener(JumpPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (
                !this.editorsManager.isInEditor(event.getPlayer()) &&
                        !this.config.get(Key.PLATES_PROTECTED) ||
                        !this.jumpManager.getProtectedWorlds().contains(event.getBlock().getWorld())
        ) return;

        Location loc = event.getBlock().getLocation();
        Location top = loc.clone();
        top.setY(top.getBlockY() + 1);

        List<Location> protectedLocations = this.jumpManager.getProtectedLocations();

        if (protectedLocations.contains(loc) || protectedLocations.contains(top)) {
            if (event.getPlayer().hasPermission(Perm.EDIT)) {
                if (!this.editorsManager.isInEditor(event.getPlayer())) {
                    event.setCancelled(true);
                    Text.EDITOR_ONLY_ACTION.send(event.getPlayer());
                } else this.editorsManager
                        .getEditor(event.getPlayer()).ifPresent(editor -> this.onInteractInEditor(event, editor));
            } else {
                event.setCancelled(true);
            }
        }
    }

    public void onInteractInEditor(@NotNull BlockBreakEvent event, @NotNull JumpEditor editor) {
        Jump jump = editor.getJump();
        Location loc = event.getBlock().getLocation();

        if (jump.getStart().map(l -> LocationUtil.isBlockLocationEqual(l, loc)).orElse(false)) {
            editor.setStart(null);
        } else if (jump.getEnd().map(l -> LocationUtil.isBlockLocationEqual(l, loc)).orElse(false)) {
            editor.setSEnd(null);
        } else if (jump.getCheckpoints().stream()
                .filter(l -> LocationUtil.isBlockLocationEqual(l, loc))
                .peek(editor::deleteCheckpoint)
                .count() == 0) {
            event.setCancelled(true);
        }
    }
}
