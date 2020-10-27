package fr.ycraft.jump.templates;

import lombok.Builder;
import lombok.Data;
import net.nowtryz.mcutils.builders.ItemBuilder;
import net.nowtryz.mcutils.inventory.AbstractGui;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.BiFunction;
import java.util.function.Consumer;

@Data
@Builder
class TemplateAction<T extends AbstractGui<?>> {
    private PatternKey key;
    private ItemProvider update;
    private Consumer<? super InventoryClickEvent> onClick;
    private BiFunction<T, ItemBuilder<?>,ItemBuilder<?>> onBuild;

    public static class TemplateActionBuilder<T extends AbstractGui<?>> {
        Template<T> template;
        public void build() {
            this.template.hook(new TemplateAction<>(key, update, onClick, onBuild));
        }

        TemplateActionBuilder<T> key(PatternKey key) {
            this.key = key;
            return this;
        }

        TemplateActionBuilder<T> template(Template<T> template) {
            this.template = template;
            return this;
        }
    }
}
