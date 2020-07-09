package fr.ycraft.jump.inventories;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.PlayerScore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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

import static fr.ycraft.jump.util.ItemLibrary.*;


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

    public BestScoresInventory(JumpPlugin plugin, Player player, Jump jump, Runnable backAction) {
        super(plugin, player, backAction);

        Inventory inventory = Bukkit.createInventory(player, 54, Text.TOP_INVENTORY_TITLE.get(jump.getName()));

        GRAY_PANE_POS.forEach(i -> inventory.setItem(i, GRAY_FILLER));
        BLACK_PANE_POS.forEach(i -> inventory.setItem(i, BLACK_FILLER));

        ItemStack emptySlot = new ItemStack(Material.SKULL_ITEM);
        ItemMeta itemMeta = emptySlot.getItemMeta();
        itemMeta.setDisplayName(Text.EMPTY_SCORE.get());
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

            inventory.setItem(PLAYER_POS.get(i), skull);
        } else {
            inventory.setItem(PLAYER_POS.get(i), emptySlot);
        }

        inventory.setItem(DIAMOND_POS, DIAMOND);
        inventory.setItem(GOLD_POS, GOLD);
        inventory.setItem(IRON_POS, IRON);
        inventory.setItem(MAGENTA_POS, ENCHANTED_FILLER);

        // if an inventory can handle a back arrow
        if (backAction != null) {
            inventory.setItem(49, BACK);
            super.addClickableItem(BACK, super::onBack);
        }

        this.setInventory(inventory);
    }
}
