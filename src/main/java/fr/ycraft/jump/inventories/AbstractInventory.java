package fr.ycraft.jump.inventories;

import fr.ycraft.jump.JumpPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class AbstractInventory implements Listener {
    private final Map<ItemStack, Consumer<? super InventoryClickEvent>> clickableItems = new HashMap<>();
    protected final Runnable backAction;
    protected final JumpPlugin plugin;
    protected final Player player;
    private Inventory inventory;

    public AbstractInventory(JumpPlugin plugin, Player player) {
        this.backAction = null;
        this.plugin = plugin;
        this.player = player;
    }

    public AbstractInventory(JumpPlugin plugin, Player player, Runnable backAction) {
        this.backAction = backAction;
        this.plugin = plugin;
        this.player = player;
    }

    public void onClick(InventoryClickEvent event) {
        if (ClickType.DOUBLE_CLICK.equals(event.getClick()) && this.player.equals(event.getWhoClicked())) {
            event.setCancelled(true);
            return;
        }

        if (!this.inventory.equals(event.getClickedInventory())) return;
        event.setCancelled(true);

        Optional.ofNullable(event.getCurrentItem())
                .filter(itemStack -> !itemStack.getType().equals(Material.AIR))
                .map(this.clickableItems::get)
                // Schedule task to enable inventory actions as proposed in InventoryClickEvent documentation
                .ifPresent(c -> Bukkit.getScheduler().runTask(this.plugin, () -> c.accept(event)));
    }

    public void close() {}

    public final void closeInventory() {
        this.player.closeInventory();
        this.close();
    }

    protected final void setInventory(Inventory inventory) {
        this.inventory = inventory;
        this.plugin.getInventoryListener().register(this, inventory);
        this.player.openInventory(inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }

    protected final void addClickableItem(ItemStack item, Consumer<? super InventoryClickEvent> consumer) {
        this.clickableItems.put(item, consumer);
    }

    protected void onBack(Event event) {
        this.closeInventory();
        Optional.ofNullable(this.backAction).ifPresent(Runnable::run);
    }
}
