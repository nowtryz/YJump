package fr.ycraft.jump.listeners;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.inventories.AbstractInventory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class InventoryListener extends AbstractListener {
    private final Map<Inventory, AbstractInventory> inventories = new HashMap<>();

    @Inject
    public InventoryListener(JumpPlugin plugin) {
        super(plugin);
    }

    public void register(AbstractInventory inventoryInstance, Inventory inventory) {
        if (this.inventories.isEmpty()) this.register();
        this.inventories.put(inventory, inventoryInstance);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Optional.ofNullable(this.inventories.get(event.getInventory())).ifPresent(AbstractInventory::close);
        this.inventories.remove(event.getInventory());
        if (this.inventories.isEmpty()) this.unRegister();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Optional.ofNullable(this.inventories.get(event.getClickedInventory()))
                .ifPresent(inventory -> inventory.onClick(event));
    }
}
