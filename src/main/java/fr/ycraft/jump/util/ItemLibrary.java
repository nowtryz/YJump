package fr.ycraft.jump.util;

import fr.ycraft.jump.Text;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemLibrary {
    public static final ItemStack WHITE_FILLER = new ItemStack(Material.STAINED_GLASS_PANE);
    public static final ItemStack BLACK_FILLER = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
    public static final ItemStack GRAY_FILLER = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
    public static final ItemStack ENCHANTED_FILLER = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2);
    public static final ItemStack DIAMOND = new ItemStack(Material.DIAMOND_BLOCK);
    public static final ItemStack GOLD = new ItemStack(Material.GOLD_BLOCK);
    public static final ItemStack IRON = new ItemStack(Material.IRON_BLOCK);

    public static final ItemStack BACK = new ItemStack(Material.ARROW);

    static {
        ItemMeta itemMeta = ENCHANTED_FILLER.getItemMeta();
        itemMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.setDisplayName(" ");
        ENCHANTED_FILLER.setItemMeta(itemMeta);
    }

    static {
        ItemStackUtil.setName(WHITE_FILLER, StringUtils.SPACE);
        ItemStackUtil.setName(BLACK_FILLER, " ");
        ItemStackUtil.setName(GRAY_FILLER, " ");
        ItemStackUtil.setName(DIAMOND, "#1");
        ItemStackUtil.setName(GOLD, "#2");
        ItemStackUtil.setName(IRON, "#3");
    }

    public static void init() {
        ItemStackUtil.setName(BACK, Text.BACK.get());

    }
}
