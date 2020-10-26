package fr.ycraft.jump.templates;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.nowtryz.mcutils.inventory.AbstractGui;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class TemplatedGuiBuilder {
    private final Pattern pattern;
    private final AbstractGui<?> gui;

    public TemplatedGuiBuilder name(String name) {
        this.gui.createInventory(pattern.getSize(), name);
        this.gui.getInventory().setContents(pattern.toInventory());
        return this;
    }

    public final TemplatedGuiBuilder hook(String name, Consumer<? super InventoryClickEvent> action, ItemProvider provider) {
        this.pattern.getHook(name).ifPresent(hook -> {
            ItemStack item = provider.build(hook.builder()).build();
            this.hook(hook, action, item);
        });

        return this;
    }

    public final TemplatedGuiBuilder hook(String name, ItemStack item) {
        this.pattern.getHook(name).ifPresent(hook -> this.hook(hook, item));
        return this;
    }

    public final TemplatedGuiBuilder hook(String name, ItemProvider provider) {
        this.pattern.getHook(name).ifPresent(hook -> {
            ItemStack item = provider.build(hook.builder()).build();
            this.hook(hook, item);
        });

        return this;
    }

    public final TemplatedGuiBuilder hook(String name, Consumer<? super InventoryClickEvent> action, ItemStack item) {
        this.pattern.getHook(name).ifPresent(hook -> this.hook(hook, action, item));
        return this;
    }

    public final TemplatedGuiBuilder hook(String name, Consumer<? super InventoryClickEvent> action) {
        this.pattern.getHook(name).ifPresent(hook -> this.hook(hook, action, hook.getItem()));
        return this;
    }

    public TemplatedGuiBuilder hook(PatternKey hook, Consumer<? super InventoryClickEvent> action, ItemStack item) {
        if (hook.getPositions().length == 0) return this;
        Inventory inventory = this.gui.getInventory();

        for (int pos : hook.getPositions()) inventory.setItem(pos, item);
        this.gui.addClickableItem(inventory.getItem(hook.getPositions()[0]), action);

        return this;
    }

    public TemplatedGuiBuilder hook(PatternKey hook, ItemStack item) {
        if (hook.getPositions().length == 0) return this;
        for (int pos : hook.getPositions()) gui.getInventory().setItem(pos, item);
        return this;
    }

    /**
     * Hook an item to the "back' action of the gui. It will only add the hook if the gui has a previous
     * gui registered
     * @param hookName the name of the hook in the pattern
     * @param provider a provider to add element to the item from the pattern
     * @return this builder
     */
    public final TemplatedGuiBuilder hookBack(String hookName, ItemProvider provider) {
        if (this.gui.hasPrevious()) this.hook(hookName, this.gui::onBack, provider);
        return this;
    }

    /**
     * Hook to "back' action of the gui without updating the item. It will only add the hook if the gui has a previous
     * gui registered
     * @param hookName the name of the hook in the pattern
     * @return this builder
     */
    public final TemplatedGuiBuilder hookBack(String hookName) {
        if (gui.hasPrevious())this.hook(hookName, gui::onBack);
        return this;
    }

    public final TemplatedGuiBuilder hookIf(boolean condition, Consumer<TemplatedGuiBuilder> consumer) {
        if (condition) consumer.accept(this);
        return this;
    }
}
