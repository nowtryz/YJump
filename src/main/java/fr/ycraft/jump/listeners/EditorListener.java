package fr.ycraft.jump.listeners;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.commands.Perm;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class EditorListener implements Listener {
    private final JumpPlugin plugin;

    public EditorListener(JumpPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreakInEditor(BlockBreakEvent event) {
        // Avoid interaction in editors
        if (this.plugin.getConfigProvider().isCreativeEditor() &&
                this.plugin.getEditorsManager().getEditor(event.getPlayer())
                        .filter(e -> !Perm.EDITOR_INTERACTIONS.isHeldBy(event.getPlayer()))
                        .isPresent()) {
            event.setCancelled(true);
        }
    }
}
