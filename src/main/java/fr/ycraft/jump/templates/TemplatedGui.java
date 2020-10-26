package fr.ycraft.jump.templates;

import net.nowtryz.mcutils.api.Gui;
import net.nowtryz.mcutils.api.Plugin;
import net.nowtryz.mcutils.inventory.AbstractGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class TemplatedGui<P extends Plugin> extends AbstractGui<P> {
    private final Pattern pattern;

    public TemplatedGui(P plugin, Player player, Pattern pattern, String name) {
        super(plugin, player);
        this.pattern = pattern;

        this.setInventory(Bukkit.createInventory(player, pattern.getSize(), name));
        this.getInventory().setContents(pattern.toInventory());
    }

    public TemplatedGui(P plugin, Player player, Pattern pattern, String name, Gui previousInventory) {
        super(plugin, player, previousInventory);
        this.pattern = pattern;

        this.setInventory(Bukkit.createInventory(player, pattern.getSize(), name));
        this.getInventory().setContents(pattern.toInventory());
    }

    public final void hook(String name, Consumer<? super InventoryClickEvent> action, ItemProvider provider) {
        this.pattern.getHook(name).ifPresent(hook -> {
            ItemStack item = provider.build(hook.builder()).build();
            this.hook(hook, action, item);
        });
    }

    public final void hook(String name, ItemStack item) {
        this.pattern.getHook(name).ifPresent(hook -> this.hook(hook, item));
    }

    public final void hook(String name, Consumer<? super InventoryClickEvent> action, ItemStack item) {
        this.pattern.getHook(name).ifPresent(hook -> this.hook(hook, action, item));
    }

    public final void hook(String name, Consumer<? super InventoryClickEvent> action) {
        this.pattern.getHook(name).ifPresent(hook -> this.hook(hook, action, hook.getItem()));
    }

    private void hook(PatternKey hook, Consumer<? super InventoryClickEvent> action, ItemStack item) {
        if (hook.getPositions().length == 0) return;

        for (int pos : hook.getPositions()) super.getInventory().setItem(pos, item);
        super.addClickableItem(super.getInventory().getItem(hook.getPositions()[0]), action);
    }

    private void hook(PatternKey hook, ItemStack item) {
        if (hook.getPositions().length == 0) return;
        for (int pos : hook.getPositions()) super.getInventory().setItem(pos, item);
    }

    public final void hookBack(String hookName, ItemProvider provider) {
        Consumer<InventoryClickEvent> onBack = super::onBack;
        this.hook(hookName, super::onBack, provider);
    }

    public final void hookBack(String hookName) {
        this.hook(hookName, super::onBack);
    }
}
