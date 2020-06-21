package fr.ycraft.jump.inventories;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractInventory implements Listener {
    protected static final ItemStack back = new ItemStack(Material.ARROW);
    public static void init() {
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(Text.BACK.get());
        back.setItemMeta(backMeta);
    }

    private final Map<ItemStack, Consumer<? super InventoryClickEvent>> clickableItems = new HashMap<>();
    protected final Runnable backAction;
    protected final JumpPlugin plugin;
    protected final Player player;
    protected Inventory inventory;

    public AbstractInventory(JumpPlugin plugin, Player player) {
        this.backAction = null;
        this.plugin = plugin;
        this.player = player;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public AbstractInventory(JumpPlugin plugin, Player player, Runnable backAction) {
        this.backAction = backAction;
        this.plugin = plugin;
        this.player = player;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(this.inventory)) HandlerList.unregisterAll(this);
    }

    @EventHandler
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
                .ifPresent(c -> c.accept(event));
    }

    public void close() {
        if (this.inventory != null && this.inventory.equals(this.player.getOpenInventory().getTopInventory())) {
            this.player.closeInventory();
        }
        HandlerList.unregisterAll(this);
    }

    protected final void addClickableItem(ItemStack item, Consumer<? super InventoryClickEvent> consumer) {
        this.clickableItems.put(item, consumer);
    }

    protected void onBack(Event event) {
        this.close();
        Optional.ofNullable(this.backAction).ifPresent(Runnable::run);
    }
}
