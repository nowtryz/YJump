package fr.ycraft.jump.inventories;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.entity.Jump;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.ycraft.jump.util.ItemLibrary.BACK;
import static fr.ycraft.jump.util.ItemLibrary.WHITE_FILLER;

public class InfoInventory extends AbstractInventory {
    private static final int INVENTORY_SIZE = 54;
    private static final List<Integer> panePos = Stream.iterate(0, i->i+1)
            .limit(INVENTORY_SIZE)
            .filter(i -> i/9 == 0 || i/9 == 2 || i/9 == 5 || i%9 == 0 || i%9 == 8 || i == 12 || i == 14)
            .collect(Collectors.toList());
    private static final List<Integer> checkpointsPos = Stream.iterate(3*9, i->i+1)
            .limit(5*9)
            .filter(i -> i%9 != 0 && i%9 != 8)
            .collect(Collectors.toList());

    private final Jump jump;

    public InfoInventory(JumpPlugin plugin, Player player, Jump jump) {
        this(plugin, player, jump, null);
    }

    public InfoInventory(JumpPlugin plugin, Player player, Jump jump, Runnable runnable) {
        super(plugin, player, runnable);
        this.jump = jump;

        Inventory inventory = Bukkit.createInventory(player, INVENTORY_SIZE, Text.INFO_TITLE.get(jump.getName()));
        List<String> notSet = Arrays.asList(Text.INFO_POINT_NOT_SET_LORE.get().split(StringUtils.LF));
        ItemStack icon = jump.getItem().clone();
        ItemStack spawn = new ItemStack(Material.COMPASS);
        ItemStack start = new ItemStack(plugin.getConfigProvider().getStartMaterial());
        ItemStack end = new ItemStack(plugin.getConfigProvider().getEndMaterial());
        ItemStack fall = new ItemStack(Material.BED, 1, (short) 14);
        ItemMeta spawnMeta = spawn.getItemMeta();
        ItemMeta startMeta = start.getItemMeta();
        ItemMeta endMeta = end.getItemMeta();
        ItemMeta fallMeta = fall.getItemMeta();

        spawnMeta.setDisplayName(Text.INFO_SPAWN_NAME.get());
        startMeta.setDisplayName(Text.INFO_START_NAME.get());
        endMeta.setDisplayName(Text.INFO_END_NAME.get());
        fallMeta.setDisplayName(Text.INFO_FALL_NAME.get());
        fallMeta.setLore(Arrays.asList(Text.INFO_FALL_LORE
                .get(plugin.getConfigProvider().getMaxFallDistance())
                .split(StringUtils.LF)));

        jump.getSpawn().ifPresent(location -> spawnMeta.setLore(Arrays.asList(Text.INFO_POINT_SET_LORE.get(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        ).split(StringUtils.LF))));
        jump.getStart().ifPresent(location -> startMeta.setLore(Arrays.asList(Text.INFO_POINT_SET_LORE.get(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        ).split(StringUtils.LF))));
        jump.getEnd().ifPresent(location -> endMeta.setLore(Arrays.asList(Text.INFO_POINT_SET_LORE.get(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        ).split(StringUtils.LF))));

        if (!jump.getSpawn().isPresent()) spawnMeta.setLore(notSet);
        if (!jump.getStart().isPresent()) startMeta.setLore(notSet);
        if (!jump.getEnd().isPresent())   endMeta.setLore(notSet);

        spawn.setItemMeta(spawnMeta);
        start.setItemMeta(startMeta);
        end.setItemMeta(endMeta);
        fall.setItemMeta(fallMeta);

        inventory.setItem(10, icon);
        inventory.setItem(11, fall);
        inventory.setItem(13, spawn);
        inventory.setItem(15, start);
        inventory.setItem(16, end);

        List<Location> checkpoints = jump.getCheckpoints();
        for (int i = 0; i < checkpoints.size() % checkpointsPos.size(); i++) {
            Location location = checkpoints.get(i);
            ItemStack checkpoint = new ItemStack(plugin.getConfigProvider().getCheckpointMaterial());
            ItemMeta itemMeta = checkpoint.getItemMeta();

            itemMeta.setDisplayName(Text.INFO_CHECKPOINT_NAME.get());
            itemMeta.setLore(Arrays.asList(Text.INFO_POINT_SET_LORE.get(
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ()
            ).split(StringUtils.LF)));
            checkpoint.setItemMeta(itemMeta);

            inventory.setItem(28 + i, checkpoint);
            this.addClickableItem(checkpoint, event -> player.teleport(location));
        }


        this.addClickableItem(spawn, this::tpToSpawn);
        this.addClickableItem(start, this::tpToStart);
        this.addClickableItem(end,   this::tpToEnd);
        panePos.forEach(i -> inventory.setItem(i, WHITE_FILLER));

        // if an inventory can handle a back arrow
        if (runnable != null) {
            inventory.setItem(49, BACK);
            super.addClickableItem(BACK, super::onBack);
        }

        this.setInventory(inventory);
    }

    public void tpToSpawn(InventoryClickEvent event) {
        this.jump.getSpawn().ifPresent(player::teleport);
    }

    public void tpToStart(InventoryClickEvent event) {
        this.jump.getSpawn().ifPresent(player::teleport);
    }

    public void tpToEnd(InventoryClickEvent event) {
        this.jump.getEnd().ifPresent(player::teleport);
    }
}
