package fr.ycraft.jump.listeners;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.commands.Perm;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class EditorListener extends AbstractListener {
    public EditorListener(JumpPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreakInEditor(BlockBreakEvent event) {
        this.cancelEventIfNeeded(event, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreakInEditor(BlockPlaceEvent event) {
        this.cancelEventIfNeeded(event, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        this.cancelEventIfNeeded(event, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCreativeGive(InventoryCreativeEvent event) {
        HumanEntity human = event.getWhoClicked();
        if (human instanceof Player) this.cancelEventIfNeeded(event, (Player) human);
    }

    public void cancelEventIfNeeded(Cancellable event, Player player) {
        // Avoid interaction in editors
        if (this.plugin.getConfigProvider().isCreativeEditor() &&
                this.plugin.getEditorsManager().getEditor(player)
                        .filter(e -> !Perm.EDITOR_INTERACTIONS.isHeldBy(player))
                        .isPresent()) {
            event.setCancelled(true);
        }
    }
}
