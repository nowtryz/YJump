package fr.ycraft.jump.util;

import fr.ycraft.jump.Text;
import net.nowtryz.mcutils.ItemStackUtil;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static net.nowtryz.mcutils.builders.ItemBuilder.create;
import static net.nowtryz.mcutils.builders.ItemBuilder.createGlassPane;

public class ItemLibrary {
    public static final ItemStack WHITE_FILLER = create(Material.STAINED_GLASS_PANE).dropName().build();
    public static final ItemStack BLACK_FILLER = createGlassPane(DyeColor.BLACK).dropName().build();
    public static final ItemStack GRAY_FILLER = createGlassPane(DyeColor.GRAY).dropName().build();
    public static final ItemStack ENCHANTED_FILLER = createGlassPane(DyeColor.MAGENTA).dropName().setGlowing().build();
    public static final ItemStack DIAMOND = create(Material.DIAMOND_BLOCK).setDisplayName("#1").build();
    public static final ItemStack GOLD = create(Material.GOLD_BLOCK).setDisplayName("#1").build();
    public static final ItemStack IRON = create(Material.IRON_BLOCK).setDisplayName("#1").build();

    public static final ItemStack BACK = new ItemStack(Material.ARROW);

    public static void init() {
        ItemStackUtil.setName(BACK, Text.BACK.get());

    }
}
