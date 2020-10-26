package fr.ycraft.jump.inventories;

import com.google.inject.assistedinject.Assisted;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.JumpPlayer;
import fr.ycraft.jump.entity.TimeScore;
import fr.ycraft.jump.manager.JumpManager;
import fr.ycraft.jump.templates.Pattern;
import fr.ycraft.jump.templates.TemplatedPaginatedGui;
import net.nowtryz.mcutils.builders.ItemBuilder;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

public class ListInventory extends TemplatedPaginatedGui<JumpPlugin, Jump> {
    private final JumpPlayer jumpPlayer;
    private final JumpInventory.Factory factory;

    @Inject
    ListInventory(JumpPlugin plugin,
                  JumpManager manager,
                  JumpInventory.Factory factory,
                  @Named("LIST") Pattern pattern,
                  @Assisted JumpPlayer jumpPlayer,
                  @Assisted Player player) {
        super(plugin, player, null, pattern, Text.JUMP_LIST_INVENTORY.get());
        this.jumpPlayer = jumpPlayer;
        this.factory = factory;

        // Pagination
        super.setHooks("next", "previous", "jumps");
        super.setValues(manager.getJumps().values());
    }

    @Override
    protected @NotNull ItemStack createItemForObject(ItemBuilder<?> ignored, Jump jump) {
        List<TimeScore> scores = this.jumpPlayer.get(jump);
        ItemBuilder<ItemMeta> builder = ItemBuilder.from(jump.getItem())
                .setDisplayName(Text.JUMP_LIST_HEADER, jump.getName());

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
            builder.setLore(Text.JUMP_LIST_NEVER_DONE_LORE, description, distance);
        } else {
            TimeScore score = scores.get(0);
            builder.setLore(
                        Text.JUMP_LIST_DONE_LORE,
                        description,
                        distance,
                        score.getMinutes(),
                        score.getSeconds(),
                        score.getMillis())
                    .setGlowing();
        }

        return builder.build();
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
    protected void onClick(InventoryClickEvent event, Jump jump) {
        this.factory.create(this.player, this.jumpPlayer, jump, this).open();
    }

    public interface Factory {
        ListInventory create(JumpPlayer jumpPlayer, Player player);
    }
}
