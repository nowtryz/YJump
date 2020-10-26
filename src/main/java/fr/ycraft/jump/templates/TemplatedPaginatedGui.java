package fr.ycraft.jump.templates;

import lombok.Getter;
import net.nowtryz.mcutils.SchedulerUtil;
import net.nowtryz.mcutils.api.Gui;
import net.nowtryz.mcutils.api.Plugin;
import net.nowtryz.mcutils.builders.ItemBuilder;
import net.nowtryz.mcutils.inventory.AbstractGui;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class TemplatedPaginatedGui<P extends Plugin, V> extends AbstractGui<P> {
    private final Pattern pattern;
    protected final TemplatedGuiBuilder builder;
    private ItemStack previous, next;
    private PatternKey previousKey, nextKey;
    private PatternKey paginatedKey;
    @Getter private int page = 0, count;
    private int[] availablePos;
    private List<V> values;
    private Map<V,ItemStack> items = new HashMap<>();

//    public TemplatedPaginatedGui(P plugin, Player player, Collection<V> values, int previousPos, int nextPos) {
//        super(plugin, player);
//        this.values = new ArrayList<>(values);
//        this.previousPos = previousPos;
//        this.nexPos = nextPos;
//        this.init();
//    }

    public TemplatedPaginatedGui(P plugin, Player player, Gui previousInventory, Pattern pattern, String name) {
        super(plugin, player, previousInventory);
        this.pattern = pattern;
        this.builder = pattern.builder(this);
        this.builder.name(name);
    }

    protected final void fill() {
        int first = this.page * this.availablePos.length;
        List<V> pageContent = this.values.subList(
                first,
                Math.min(this.values.size(), first + this.availablePos.length)
        );

        int i = 0;

        for (; i < pageContent.size(); i++) {
            V object = pageContent.get(i);
            ItemStack item = this.getItem(object);
            int pos = this.availablePos[i];

            // will override handler for the same item if already placed
            this.addClickableItem(pos, item, event -> this.onClick(event, object));
        }

        if (i < this.availablePos.length) {
            final int finalI = i;
            SchedulerUtil.runOnPrimary(this.plugin, () -> {
                for (int j = finalI; j < this.availablePos.length; j++) {
                    this.getInventory().setItem(this.availablePos[j], this.paginatedKey.getFallback());
                }
            });
        }

        this.removeClickableItem(this.previous);
        this.removeClickableItem(this.next);


        if (this.page > 0) {
            this.previous = this.buildPreviousIcon(this.previousKey.builder());
            this.builder.hook(this.previousKey, event -> this.setPage(this.page - 1), this.previous);
        } else {
            this.builder.hook(this.previousKey, this.previousKey.getFallback());
        }

        if (this.page < this.count - 1) {
            this.next = this.buildNextIcon(this.nextKey.builder());
            this.builder.hook(this.nextKey, event -> this.setPage(this.page + 1), this.next);
        } else {
            this.builder.hook(this.nextKey, this.nextKey.getFallback());
        }
    }

    /**
     * Must be before after {@link #setValues(Collection)}
     * @param nextHook the name of the hook for the next item
     * @param previousHook the name of the hook for the previous item
     * @param paginatedHook the name of the hook for available positions
     */
    protected final void setHooks(String nextHook, String previousHook, String paginatedHook) {
        Optional<PatternKey> nextKey = this.pattern.getHook(nextHook);
        Optional<PatternKey> previousKey = this.pattern.getHook(previousHook);
        Optional<PatternKey> paginatedKey = this.pattern.getHook(paginatedHook);

        assert nextKey.isPresent();
        assert previousKey.isPresent();
        assert paginatedKey.isPresent();

        this.nextKey = nextKey.get();
        this.previousKey = previousKey.get();
        this.paginatedKey = paginatedKey.get();

        this.availablePos = this.paginatedKey.getPositions();
    }

    protected final void setPage(int page) {
        if (page >= this.count) this.page = this.count - 1;
        else this.page = Math.max(page, 0);

        if (this.isOpen()) this.fill();
    }

    protected final void setValues(Collection<V> values) {
        this.values = new ArrayList<>(values);

        this.count = Math.max((int) Math.ceil((double) this.values.size() / this.availablePos.length), 1);
        this.setPage(this.page); // update page to fit the count
    }

    private ItemStack getItem(V object) {
        if (this.items.containsKey(object)) return this.items.get(object);


        ItemStack item = this.createItemForObject(this.paginatedKey.safeBuilder(), object);
        this.items.put(object, item);
        return item;
    }

    @Override
    public final void onOpen() {
        super.onOpen();
        this.fill();
    }

    /**
     * Create an icon based on the page number for the previous page
     * @return the icon
     */
    @NotNull
    protected ItemStack buildPreviousIcon(ItemBuilder<?> builder) {
        return builder.build();
    }

    /**
     * Create an icon based on the page number for the new page
     * @return the icon
     */
    @NotNull
    protected ItemStack buildNextIcon(ItemBuilder<?> builder) {
        return builder.build();
    }

    /**
     * Create the icon bound to the object in the inventory
     * @param builder the item builder from the pattern
     * @param object the object
     * @return the item to bind
     */
    @NotNull
    protected abstract ItemStack createItemForObject(ItemBuilder<?> builder, V object);

    /**
     * Handle click on an item
     * @param event the click event
     * @param object the object bound to the clicked item
     */
    protected void onClick(InventoryClickEvent event, V object) {}
}
