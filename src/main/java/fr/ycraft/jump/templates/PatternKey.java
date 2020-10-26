package fr.ycraft.jump.templates;

import lombok.Value;
import net.nowtryz.mcutils.builders.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Value
public class PatternKey {
    String key;
    int[] positions;
    ItemStack item;
    @Nullable
    ItemStack fallback;

    public ItemBuilder<?> builder() {
        return ItemBuilder.from(this.item);
    }

    public boolean isBuildable() {
        return item != null && item.getType() != Material.AIR;
    }

    public ItemBuilder<?> safeBuilder() {
        return this.isBuildable() ? this.builder() : ItemBuilder.create(Material.STONE);
    }

    public @Nullable ItemStack getFallback() {
        return this.fallback == null ? this.item : this.fallback;
    }

    /**
     * Weather or not this key is present on the pattern
     * @return true if the key is present on the pattern, false otherwise
     */
    public boolean isPresent() {
        return this.positions.length != 0;
    }
}
