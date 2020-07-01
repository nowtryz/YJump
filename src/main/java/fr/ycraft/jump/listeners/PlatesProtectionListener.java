package fr.ycraft.jump.listeners;

import fr.ycraft.jump.JumpEditor;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.commands.Perm;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlatesProtectionListener extends AbstractListener {
    public PlatesProtectionListener(JumpPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (
                !this.plugin.getEditorsManager().isInEditor(event.getPlayer()) &&
                        !this.plugin.getConfigProvider().isPlatesProtected() ||
                        !this.plugin.getJumpManager().getProtectedWorlds().contains(event.getBlock().getWorld())
        ) return;

        Location loc = event.getBlock().getLocation();
        Location top = loc.clone();
        top.setY(top.getBlockY() + 1);

        List<Location> protectedLocations = this.plugin.getJumpManager().getProtectedLocations();

        if (protectedLocations.contains(loc) || protectedLocations.contains(top)) {
            if (Perm.EDIT.isHeldBy(event.getPlayer())) {
                if (!this.plugin.getEditorsManager().isInEditor(event.getPlayer())) {
                    event.setCancelled(true);
                    Text.EDITOR_ONLY_ACTION.send(event.getPlayer());
                } else this.plugin.getEditorsManager()
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
