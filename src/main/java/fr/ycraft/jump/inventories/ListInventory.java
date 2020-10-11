package fr.ycraft.jump.inventories;

import com.google.inject.assistedinject.Assisted;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.JumpPlayer;
import fr.ycraft.jump.entity.TimeScore;
import net.nowtryz.mcutils.inventory.AbstractGui;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public class ListInventory extends AbstractGui<JumpPlugin> {
    private final JumpPlayer jumpPlayer;
    private final JumpInventory.Factory factory;

    @Inject
    ListInventory(JumpPlugin plugin,
                  JumpInventory.Factory factory,
                  @Assisted JumpPlayer jumpPlayer,
                  @Assisted Player player) {
        super(plugin, player);
        this.jumpPlayer = jumpPlayer;
        this.factory = factory;

        int jumpCount = plugin.getJumpManager().getJumps().size();
        int size = jumpCount - jumpCount % 9 + 9;
        Inventory inventory = Bukkit.createInventory(player, size, Text.JUMP_LIST_INVENTORY.get());
        plugin.getJumpManager()
                .getJumps()
                .values()
                .stream()
                .filter(jump -> jump.getStart().isPresent())
                .map(this::jumpToItem)
                .peek(pair -> registerJump(pair.getRight(), pair.getLeft()))
                .map(Pair::getRight)
                .forEach(inventory::addItem);

        this.setInventory(inventory);
    }

    private Pair<Jump, ItemStack> jumpToItem(Jump jump) {
        List<TimeScore> scores = this.jumpPlayer.get(jump);
        ItemStack itemStack = jump.getItem().clone();
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(Text.JUMP_LIST_HEADER.get(jump.getName()));

        String description = jump.getDescription()
                .map(desc -> WordUtils.wrap(
                        desc,
                        this.plugin.getConfigProvider().get(Key.DESCRIPTION_WRAP_LENGTH),
                        StringUtils.LF, false))
                .map(desc -> StringUtils.LF + desc + StringUtils.LF)
                .orElse(StringUtils.EMPTY);

        Double distance = jump.getStart()
                .filter(l -> player.getWorld().equals(l.getWorld()))
                .map(this.player.getLocation()::distance)
                // .map(d -> Math.round(d * 100.0) / 100.0)
                .orElse(0d);

        if (scores.isEmpty()) {
            itemMeta.setLore(Arrays.asList(Text.JUMP_LIST_NEVER_DONE_LORE.get(description, distance).split(StringUtils.LF)));
        } else {
            TimeScore score = scores.get(0);
            itemMeta.setLore(Arrays.asList(Text.JUMP_LIST_DONE_LORE.get(
                description,
                distance,
                score.getMinutes(),
                score.getSeconds(),
                score.getMillis()
            ).split(StringUtils.LF)));
            itemMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        itemStack.setItemMeta(itemMeta);
        return new ImmutablePair<>(jump, itemStack);
    }

    private void registerJump(ItemStack itemStack, Jump jump) {
        super.addClickableItem(itemStack, event ->
                this.factory.create(this.player, this.jumpPlayer, jump, this).open());
    }

    public interface Factory {
        ListInventory create(JumpPlayer jumpPlayer, Player player);
    }
}
