package fr.ycraft.jump.inventories;

import com.google.inject.assistedinject.Assisted;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.Jump;
import net.nowtryz.mcutils.api.Gui;
import net.nowtryz.mcutils.builders.ItemBuilder;
import net.nowtryz.mcutils.inventory.AbstractGui;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.ycraft.jump.util.ItemLibrary.BACK;
import static fr.ycraft.jump.util.ItemLibrary.WHITE_FILLER;
import static net.nowtryz.mcutils.builders.ItemBuilder.create;

public class InfoInventory extends AbstractGui<JumpPlugin> {
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

    @Inject
    InfoInventory(JumpPlugin plugin,
                  Config config,
                  @Assisted Player player,
                  @Assisted Jump jump,
                  @Assisted() @Nullable Gui back) {

        super(plugin, player, back);
        this.jump = jump;

        Inventory inventory = Bukkit.createInventory(player, INVENTORY_SIZE, Text.INFO_TITLE.get(jump.getName()));
        this.setInventory(inventory);

        List<String> notSet = Arrays.asList(Text.INFO_POINT_NOT_SET_LORE.get().split(StringUtils.LF));
        ItemBuilder<?> spawn = create(Material.COMPASS)
                .setDisplayName(Text.INFO_SPAWN_NAME);
        ItemBuilder<?> start = create(config.get(Key.START_MATERIAL))
                .setDisplayName(Text.INFO_START_NAME);
        ItemBuilder<?> end = create(config.get(Key.END_MATERIAL))
                .setDisplayName(Text.INFO_END_NAME);

        jump.getSpawn().ifPresent(location -> spawn.setLore(
                Text.INFO_POINT_SET_LORE,
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        ));
        jump.getStart().ifPresent(location -> start.setLore(
                Text.INFO_POINT_SET_LORE,
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        ));
        jump.getEnd().ifPresent(location -> end.setLore(
                Text.INFO_POINT_SET_LORE,
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        ));

        if (!jump.getSpawn().isPresent()) spawn.setLore(notSet);
        if (!jump.getStart().isPresent()) start.setLore(notSet);
        if (!jump.getEnd().isPresent())   end.setLore(notSet);


        inventory.setItem(10, jump.getItem().clone());
        inventory.setItem(11, create(Material.BED)
                .setWoolColor(DyeColor.WHITE)
                .setDisplayName(Text.INFO_FALL_NAME)
                .setLore(Text.INFO_FALL_LORE, config.get(Key.MAX_FALL_DISTANCE))
                .build());
        this.addClickableItem(13, spawn.build(), this::tpToSpawn);
        this.addClickableItem(15, start.build(), this::tpToStart);
        this.addClickableItem(16, end.build(),   this::tpToEnd);

        List<Location> checkpoints = jump.getCheckpoints();
        for (int i = 0; i < checkpoints.size() % checkpointsPos.size(); i++) {
            Location location = checkpoints.get(i);
            this.addClickableItem(28,
                    create(Optional.of(location.getBlock().getRelative(BlockFace.DOWN))
                            .map(Block::getType)
                            .filter(m -> m != Material.AIR)
                            .orElse(config.get(Key.CHECKPOINT_MATERIAL)))
                            .setDisplayName(Text.INFO_CHECKPOINT_NAME)
                            .setLore(Text.INFO_POINT_SET_LORE,
                                    location.getBlockX(),
                                    location.getBlockY(),
                                    location.getBlockZ())
                            .build(),
                    event -> player.teleport(location));
        }

        panePos.forEach(i -> inventory.setItem(i, WHITE_FILLER));

        // if an inventory can handle a back arrow
        super.registerBackItem(BACK, 49);
    }

    public void tpToSpawn(InventoryClickEvent event) {
        this.jump.getSpawn().ifPresent(player::teleport);
    }

    public void tpToStart(InventoryClickEvent event) {
        this.jump.getStart().ifPresent(player::teleport);
    }

    public void tpToEnd(InventoryClickEvent event) {
        this.jump.getEnd().ifPresent(player::teleport);
    }

    public interface Factory {
        InfoInventory create(Player player, Jump jump, @Nullable Gui back);
    }
}
