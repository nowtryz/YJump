package fr.ycraft.jump.listeners;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.commands.enums.Perm;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.manager.EditorsManager;
import net.nowtryz.mcutils.listener.AbstractListener;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EditorListener extends AbstractListener {
    private final Config config;
    private final EditorsManager editorsManager;

    @Inject
    public EditorListener(JumpPlugin plugin, Config config, EditorsManager editorsManager) {
        super(plugin);
        this.config = config;
        this.editorsManager = editorsManager;
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
        if (this.config.get(Key.CREATIVE_EDITOR) &&
                this.editorsManager.getEditor(player)
                        .filter(e -> !player.hasPermission(Perm.EDITOR_INTERACTIONS))
                        .isPresent()) {
            event.setCancelled(true);
        }
    }
}
