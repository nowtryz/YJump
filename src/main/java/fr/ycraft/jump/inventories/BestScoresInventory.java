package fr.ycraft.jump.inventories;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.PlayerScore;
import net.nowtryz.mcutils.api.Gui;
import net.nowtryz.mcutils.builders.ItemBuilder;
import net.nowtryz.mcutils.inventory.AbstractGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.ycraft.jump.util.ItemLibrary.*;


public class BestScoresInventory extends AbstractGui<JumpPlugin> {
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

    public BestScoresInventory(JumpPlugin plugin, Player player, Jump jump, Gui back) {
        super(plugin, player, back);

        Inventory inventory = Bukkit.createInventory(player, 54, Text.TOP_INVENTORY_TITLE.get(jump.getName()));
        this.setInventory(inventory);

        GRAY_PANE_POS.forEach(i -> inventory.setItem(i, GRAY_FILLER));
        BLACK_PANE_POS.forEach(i -> inventory.setItem(i, BLACK_FILLER));

        ItemStack emptySlot = ItemBuilder.create(Material.SKULL_ITEM).setDisplayName(Text.EMPTY_SCORE.get()).build();

        LinkedList<PlayerScore> bestScores = new LinkedList<>(jump.getBestScores());
        for (int i = 0; i < 10; i++) if (i < bestScores.size()) {
            PlayerScore score = bestScores.get(i);
            inventory.setItem(PLAYER_POS.get(i), ItemBuilder.skullForPlayer(score.getPlayer())
                    .setDisplayName(Text.TOP_SCORE_TITLE,
                            score.getPlayer().getName(),
                            i + 1,
                            score.getScore().getMinutes(),
                            score.getScore().getSeconds(),
                            score.getScore().getMillis())
                    .setLore(Text.TOP_SCORE_LORE,
                            score.getPlayer().getName(),
                            i + 1,
                            score.getScore().getMinutes(),
                            score.getScore().getSeconds(),
                            score.getScore().getMillis())
                    .build());
        } else {
            inventory.setItem(PLAYER_POS.get(i), emptySlot);
        }

        inventory.setItem(DIAMOND_POS, DIAMOND);
        inventory.setItem(GOLD_POS, GOLD);
        inventory.setItem(IRON_POS, IRON);
        inventory.setItem(MAGENTA_POS, ENCHANTED_FILLER);

        // if an inventory can handle a back arrow
        super.registerBackItem(BACK, 49);
    }
}
