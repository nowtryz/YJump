package fr.ycraft.jump.inventories;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.PlayerScore;
import fr.ycraft.jump.util.ItemStackUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class BestScoresInventory extends AbstractInventory {
    private static final int INVENTORY_SIZE = 54;
    private static final int DIAMOND_POS = 22, GOLD_POS = 30, IRON_POS = 32, MAGENTA_POS = 31;
    private static final int TOP1 = 13, TOP2 = 21, TOP3 = 23, TOP4 = 37;

    private static final UnaryOperator<Integer> incrementer = i -> i + 1;
    private static final Predicate<Integer> isPlayerHead = i -> i == TOP1 || i == TOP2 || i == TOP3 || (i >= TOP4 && i <= TOP4 + 6);
    private static final Predicate<Integer> isGray = i -> i%9 == 0 || i%9 == 8 || i == 1 || i == 7 || i == (INVENTORY_SIZE - 8) || i == (INVENTORY_SIZE - 2);
    private static final Predicate<Integer> isBlack = i -> !isGray.test(i) && !isPlayerHead.test(i) && i != DIAMOND_POS && i != GOLD_POS && i != IRON_POS;

    private static final List<Integer> GRAY_PANE_POS = Stream.iterate(0, incrementer)
            .limit(INVENTORY_SIZE)
            .filter(isGray)
            .collect(Collectors.toList());
    private static final List<Integer> BLACK_PANE_POS = Stream.iterate(0, incrementer)
            .limit(INVENTORY_SIZE)
            .filter(isBlack)
            .collect(Collectors.toList());
    private static final List<Integer> PLAYER_POS = Stream.iterate(0, incrementer)
            .limit(INVENTORY_SIZE)
            .filter(isPlayerHead)
            .collect(Collectors.toList());

    private static final ItemStack blackGlassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
    private static final ItemStack grayGlassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
    private static final ItemStack magentaGlassPane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 2);
    private static final ItemStack DIAMOND = new ItemStack(Material.DIAMOND_BLOCK);
    private static final ItemStack GOLD = new ItemStack(Material.GOLD_BLOCK);
    private static final ItemStack IRON = new ItemStack(Material.IRON_BLOCK);

    static {
        ItemMeta itemMeta = magentaGlassPane.getItemMeta();
        itemMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.setDisplayName(" ");
        magentaGlassPane.setItemMeta(itemMeta);
    }
    static {
        ItemStackUtil.setName(blackGlassPane, " ");
        ItemStackUtil.setName(grayGlassPane, " ");
        ItemStackUtil.setName(DIAMOND, "#1");
        ItemStackUtil.setName(GOLD, "#2");
        ItemStackUtil.setName(IRON, "#3");
    }

    public BestScoresInventory(JumpPlugin plugin, Player player, Jump jump) {
        this(plugin, player, jump, null);
    }


    public BestScoresInventory(JumpPlugin plugin, Player player, Jump jump, Runnable backAction) {
        super(plugin, player, backAction);

        this.inventory = Bukkit.createInventory(player, 54, Text.TOP_INVENTORY_TITLE.get(jump.getName()));

        GRAY_PANE_POS.forEach(i -> this.inventory.setItem(i, grayGlassPane));
        BLACK_PANE_POS.forEach(i -> this.inventory.setItem(i, blackGlassPane));

        ItemStack emptySlot = new ItemStack(Material.SKULL_ITEM);
        ItemMeta itemMeta = emptySlot.getItemMeta();
        itemMeta.setDisplayName("Empty slot"); // FIXME translate
        emptySlot.setItemMeta(itemMeta);

        LinkedList<PlayerScore> bestScores = new LinkedList<>(jump.getBestScores());
        for (int i = 0; i < 10; i++) if (i < bestScores.size()) {
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            PlayerScore score = bestScores.get(i);

            meta.setOwningPlayer(score.getPlayer());
            meta.setDisplayName(Text.TOP_SCORE_TITLE.get(
                    score.getPlayer().getName(),
                    i + 1,
                    score.getScore().getMinutes(),
                    score.getScore().getSeconds(),
                    score.getScore().getMillis()
            ));
            meta.setLore(Arrays.asList(Text.TOP_SCORE_LORE.get(
                    score.getPlayer().getName(),
                    i + 1,
                    score.getScore().getMinutes(),
                    score.getScore().getSeconds(),
                    score.getScore().getMillis()
            ).split("\n")));
            skull.setItemMeta(meta);

            this.inventory.setItem(PLAYER_POS.get(i), skull);
        } else {
            this.inventory.setItem(PLAYER_POS.get(i), emptySlot);
        }

        this.inventory.setItem(DIAMOND_POS, DIAMOND);
        this.inventory.setItem(GOLD_POS, GOLD);
        this.inventory.setItem(IRON_POS, IRON);
        this.inventory.setItem(MAGENTA_POS, magentaGlassPane);

        // if an inventory can handle a back arrow
        if (backAction != null) {
            this.inventory.setItem(49, back);
            super.addClickableItem(back, super::onBack);
        }

        player.openInventory(this.inventory);
    }
}
