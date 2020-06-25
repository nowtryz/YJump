package fr.ycraft.jump.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemStackUtil {
    public static void setName(ItemStack item, String name) {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(name);
        item.setItemMeta(itemMeta);
    }

    public static void clearEnchants(ItemStack item) {
        item.getEnchantments().keySet().forEach(item::removeEnchantment);
    }
}
