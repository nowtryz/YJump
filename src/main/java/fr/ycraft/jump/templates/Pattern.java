package fr.ycraft.jump.templates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.Value;
import net.nowtryz.mcutils.inventory.AbstractGui;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;

@Value
public class Pattern {
    ImmutableList<PatternKey> pattern;
    ImmutableMap<String, PatternKey> keys;
    Map<String, PatternKey> hooks;

    public int getSize() {
        return pattern.size();
    }

    public ItemStack[] toInventory() {
        return this.pattern.stream()
                .map(patternKey -> patternKey.getFallback() != null ? patternKey.getFallback() : patternKey.getItem())
                .toArray(ItemStack[]::new);
    }

    public Optional<PatternKey> getHook(String hook) {
        return Optional.ofNullable(this.hooks.get(hook));
    }

    public TemplatedGuiBuilder builder(AbstractGui<?> gui) {
        return new TemplatedGuiBuilder(this, gui);
    }
}
