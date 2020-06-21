package fr.ycraft.jump.inventories;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.TimeScore;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ListInventory extends AbstractInventory {
    private final static Material MATERIAL = Material.SLIME_BLOCK;

    private final Map<ItemStack, Jump> items = new LinkedHashMap<>();

    public ListInventory(JumpPlugin plugin, Player player) {
        super(plugin, player);

        int jumpCount = plugin.getJumpManager().getJumps().size();
        int size = jumpCount - jumpCount % 9 + 9;
        this.inventory = Bukkit.createInventory(player, size, Text.JUMP_LIST_INVENTORY.get());
        plugin.getJumpManager()
                .getJumps()
                .values()
                .stream()
                .map(this::jumpToItem)
                .peek(pair -> registerJump(pair.getRight(), pair.getLeft()))
                .map(Pair::getRight)
                .forEach(inventory::addItem);

        player.openInventory(inventory);
    }

    private Pair<Jump, ItemStack> jumpToItem(Jump jump) {
        List<Long> scores = this.plugin.getPlayerManager().getScores(this.player, jump);
        ItemStack itemStack = new ItemStack(MATERIAL);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(Text.JUMP_LIST_HEADER.get(jump.getName()));

        Double distance = jump.getStart()
                .filter(l -> player.getWorld().equals(l.getWorld()))
                .map(this.player.getLocation()::distance)
                // .map(d -> Math.round(d * 100.0) / 100.0)
                .orElse(0d);

        if (scores.isEmpty()) {
            itemMeta.setLore(Arrays.asList(Text.JUMP_LIST_NEVER_DONE_LORE.get(distance).split("\n")));
        }
        else {
            TimeScore score = new TimeScore(scores.get(0));
            itemMeta.setLore(Arrays.asList(Text.JUMP_LIST_DONE_LORE.get(
                distance,
                score.getMinutes(),
                score.getSeconds(),
                score.getMillis()
            ).split("\n")));
            itemMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        itemStack.setItemMeta(itemMeta);

        this.items.put(itemStack, jump);
        return new ImmutablePair<>(jump, itemStack);
    }

    private void registerJump(ItemStack itemStack, Jump jump) {
        super.addClickableItem(itemStack, event -> {
            this.close();
            new JumpInventory(this.plugin, this.player, jump, () -> new ListInventory(this.plugin, this.player));
        });
    }
}
