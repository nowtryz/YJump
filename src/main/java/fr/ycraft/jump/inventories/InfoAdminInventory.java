package fr.ycraft.jump.inventories;

import com.google.inject.assistedinject.Assisted;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.Position;
import net.nowtryz.mcutils.api.Gui;
import net.nowtryz.mcutils.builders.ItemBuilder;
import net.nowtryz.mcutils.injection.Nullable;
import net.nowtryz.mcutils.inventory.TemplatedPaginatedGui;
import net.nowtryz.mcutils.templating.Pattern;
import net.nowtryz.mcutils.templating.PatternKey;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.nowtryz.mcutils.builders.ItemBuilder.create;

public class InfoAdminInventory extends TemplatedPaginatedGui<JumpPlugin, Position> {
    private final Jump jump;
    private final Config config;

    @Inject
    InfoAdminInventory(JumpPlugin plugin,
                       Config config,
                       @Named("ADMIN") Pattern pattern,
                       @Assisted Player player,
                       @Assisted Jump jump,
                       @Assisted @Nullable Gui back) {

        super(plugin, player, back, pattern, Text.INFO_TITLE.get(jump.getName()));
        this.config = config;
        this.jump = jump;

        // Basic gui
        List<String> notSet = Arrays.asList(Text.INFO_POINT_NOT_SET_LORE.get().split(StringUtils.LF));

        @SuppressWarnings("unchecked")
        ItemBuilder<?> spawn = pattern.getHook("spawn")
                .map(PatternKey::builder)
                .map(b -> (ItemBuilder<ItemMeta>) b)
                .orElseGet(() -> create(Material.COMPASS))
                .setDisplayName(Text.INFO_SPAWN_NAME);
        ItemBuilder<?> start = create(config.get(Key.START_MATERIAL))
                .setDisplayName(Text.INFO_START_NAME);
        ItemBuilder<?> end = create(config.get(Key.END_MATERIAL))
                .setDisplayName(Text.INFO_END_NAME);

        jump.getSpawnPos().ifPresent(location -> spawn.setLore(
                Text.INFO_POINT_SET_LORE,
                location.getX(),
                location.getY(),
                location.getZ()
        ));
        jump.getStartPos().ifPresent(location -> start.setLore(
                Text.INFO_POINT_SET_LORE,
                location.getX(),
                location.getY(),
                location.getZ()
        ));
        jump.getEndPos().ifPresent(location -> end.setLore(
                Text.INFO_POINT_SET_LORE,
                location.getX(),
                location.getY(),
                location.getZ()
        ));

        if (!jump.getSpawnPos().isPresent()) spawn.setLore(notSet);
        if (!jump.getStartPos().isPresent()) start.setLore(notSet);
        if (!jump.getEndPos().isPresent())   end.setLore(notSet);

        this.builder
                .hookBack("back",  b -> b.setDisplayName(Text.BACK))
                .hookItem("icon", ItemBuilder.from(jump.getItem().clone())
                        .setDisplayName(Text.INFO_ICON_NAME)
                        .setLore(Text.INFO_ICON_LORE,
                                jump.getItem().hasItemMeta() && jump.getItem().getItemMeta().getDisplayName() != null ?
                                        jump.getItem().getItemMeta().getDisplayName() :
                                        jump.getItem().getType().name().toLowerCase().replaceAll(" ", " "))
                        .addAllItemFlags()
                        .build())
                .hookProvider("fall distance", b -> b
                        .setDisplayName(Text.INFO_FALL_NAME)
                        .setLore(Text.INFO_FALL_LORE, config.get(Key.MAX_FALL_DISTANCE)))
                .hookItem("world", create(getIcon(jump.getWorld()))
                        .setDisplayName(Text.INFO_WORLD_NAME)
                        .setLore(Text.INFO_WORLD_LORE, Optional
                                .ofNullable(jump.getWorld())
                                .map(World::getName)
                                .orElseGet(() -> Text.INFO_WORLD_NOT_SET.get(jump.getName()))
                        ).build())
                .hookAction("spawn", this::tpToSpawn, spawn.build())
                .hookAction("start", this::tpToStart, start.build())
                .hookAction("end",   this::tpToEnd,   end.build());

        // Pagination
        super.setHooks("next", "previous", "checkpoints");
        super.setValues(jump.getCheckpointsPositions());
    }

    public void teleport(Position checkpoint) {
        if (this.jump.getWorld() != null) this.player.teleport(checkpoint.toLocation(this.jump.getWorld()));
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

    @Override
    protected @NotNull ItemStack buildPreviousIcon(ItemBuilder<?> builder) {
        return builder
                .setDisplayName(Text.PREVIOUS_PAGE, this.getPage(), this.getCount())
                .build();
    }

    @Override
    protected @NotNull ItemStack buildNextIcon(ItemBuilder<?> builder) {
        return builder
                .setDisplayName(Text.NEXT_PAGE, this.getPage() + 2, this.getCount())
                .build();
    }

    @Override
    protected @NotNull ItemStack createItemForObject(ItemBuilder<?> builder, Position position) {
        return create(Optional.ofNullable(jump.getWorld())
                .map(position::toLocation)
                .map(Location::getBlock)
                .map(block -> block.getRelative(BlockFace.DOWN))
                .map(Block::getType)
                .filter(m -> m != Material.AIR)
                .orElse(this.config.get(Key.CHECKPOINT_MATERIAL)))
                .setDisplayName(Text.INFO_CHECKPOINT_NAME)
                .setLore(Text.INFO_POINT_SET_LORE,
                        position.getX(),
                        position.getY(),
                        position.getZ())
                .build();
    }

    @Override
    protected void onClick(InventoryClickEvent event, Position position) {
        this.teleport(position);
    }

    public interface Factory {
        InfoAdminInventory create(Player player, Jump jump, @Nullable Gui back);
    }

    /*
     * Static methods
     */

    private static Material getIcon(World world) {
        if (world == null) return Material.BARRIER;
        switch (world.getEnvironment()) {
            case NETHER: return Material.NETHERRACK;
            case THE_END: return Material.ENDER_STONE;
            case NORMAL:
            default:
                return Material.GRASS;
        }
    }
}
