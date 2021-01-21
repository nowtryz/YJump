package fr.ycraft.jump.inventories;

import com.google.inject.assistedinject.Assisted;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.enums.Patterns;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.injection.Patterned;
import fr.ycraft.jump.storage.Storage;
import net.nowtryz.mcutils.api.Gui;
import net.nowtryz.mcutils.injection.PluginLogger;
import net.nowtryz.mcutils.inventory.AbstractGui;
import net.nowtryz.mcutils.templating.Pattern;
import net.nowtryz.mcutils.templating.PatternKey;
import net.nowtryz.mcutils.templating.TemplatedGuiBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FallDistanceInventory extends AbstractGui<JumpPlugin> {
    private final TemplatedGuiBuilder builder;
    private final Jump jump;
    private final Logger logger;
    private final Config config;
    private final Storage storage;
    private final int initialValue;

    public interface Factory {
        FallDistanceInventory create(Jump jump, Player player, Gui previousInventory);
    }

    @Inject
    public FallDistanceInventory(JumpPlugin plugin,
                                 Storage storage,
                                 Config config,
                                 @Patterned(Patterns.FALL_DISTANCE) Pattern pattern,
                                 @PluginLogger Logger logger,
                                 @Assisted Jump jump,
                                 @Assisted Player player,
                                 @Assisted Gui previousInventory
    ) {
        super(plugin, player, previousInventory);
        this.jump = jump;
        this.config = config;
        this.logger = logger;
        this.storage = storage;
        this.initialValue = jump.getFallDistance();

        this.builder = pattern.builder(this)
                .name(Text.FALL_DISTANCE_INVENTORY_TITLE)
                .hookBack("back", b -> b.setDisplayName(Text.BACK))
                .hookAction("value", this::reset, b -> b
                        .setDisplayName(Text.FALL_DISTANCE_INVENTORY_ICON)
                        .setAmount(this.distanceToAmount(jump.getFallDistance()))
                        .setLore(Text.FALL_DISTANCE_INVENTORY_LORE, jump.getFallDistance(), config.get(Key.MAX_FALL_DISTANCE)))
                .hookAction("decrease", this::onDecrease, b -> b.setDisplayName(Text.FALL_DISTANCE_INVENTORY_DECREASE))
                .hookAction("increase", this::onIncrease, b -> b.setDisplayName(Text.FALL_DISTANCE_INVENTORY_INCREASE))
                .hookAction("decrease10", this::onDecrease10, b -> b.setDisplayName(Text.FALL_DISTANCE_INVENTORY_DECREASE_10))
                .hookAction("increase10", this::onIncrease10, b -> b.setDisplayName(Text.FALL_DISTANCE_INVENTORY_INCREASE_10));

        this.update(jump.getFallDistance());
    }

    private void update(int value) {
        if (value > 0) this.builder.reHook("value", b -> b
                .setDisplayName(Text.FALL_DISTANCE_INVENTORY_ICON)
                .setLore(Text.FALL_DISTANCE_INVENTORY_LORE, value, this.config.get(Key.MAX_FALL_DISTANCE))
                .setAmount(this.distanceToAmount(value)));
        else this.builder.fallback("value", b -> b
                .setDisplayName(Text.FALL_DISTANCE_INVENTORY_ICON)
                .setLore(Text.FALL_DISTANCE_INVENTORY_DISABLED, this.config.get(Key.MAX_FALL_DISTANCE)));
    }

    private void setValue(int input) {
        int value = Math.max(input, 0);
        this.jump.setFallDistance(value);
        this.update(value);
    }

    private int distanceToAmount(int distance) {
        return distance > 64 ? 1 : distance;
    }

    private void onDecrease() {
        this.setValue(this.jump.getFallDistance() - 1);
    }

    private void onIncrease() {
        this.setValue(this.jump.getFallDistance() + 1);
    }

    private void onDecrease10() {
        this.setValue(this.jump.getFallDistance() - 10);
    }

    private void onIncrease10() {
        this.setValue(this.jump.getFallDistance() + 10);
    }

    private void reset() {
        this.setValue(this.config.get(Key.MAX_FALL_DISTANCE));
    }

    @Override
    public void onClose() {
        super.onClose();
        if (this.initialValue != this.jump.getFallDistance()) {
            this.storage.storeJump(this.jump).whenComplete((unused, throwable) -> this.onSaved(throwable));
        }
    }

    private void onSaved(Throwable throwable) {
        if (throwable != null) {
            Text.ERROR.send(this.player);
            if (this.plugin.isProd()) {
                this.logger.severe( "Unable to save jump after fall distance edition:" + throwable.getMessage());
            } else this.logger.log(Level.SEVERE, "Unable to save jump after fall distance edition", throwable);
        } else {
            Text.SAVED.send(this.player);
        }
    }
}
