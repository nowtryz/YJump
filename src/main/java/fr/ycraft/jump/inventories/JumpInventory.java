package fr.ycraft.jump.inventories;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.TimeScore;
import fr.ycraft.jump.util.ItemStackUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JumpInventory  extends AbstractInventory {
    private static final int INVENTORY_SIZE = 54;
    private static final List<Integer> panePos = Stream.iterate(0, i->i+1)
            .limit(INVENTORY_SIZE)
            .filter(i -> i/9 == 0 || i/9 == 5 || i%9 == 0 || i%9 == 8 || i == 10 || i == 16 || i == 37 || i == 43)
            .collect(Collectors.toList());

    private static final ItemStack GLASS_PANE = new ItemStack(Material.STAINED_GLASS_PANE);
    private static final ItemStack TOP = new ItemStack(Material.WATCH);
    private static final ItemStack TP = new ItemStack(Material.COMPASS);
    private static final int TOP_POS = 21, SKULL_POS = 23, TP_POS = 31;

    public static void init(JumpPlugin plugin) {
        ItemStackUtil.setName(TP, Text.JUMP_INVENTORY_TP.get());
        ItemStackUtil.setName(TOP, Text.JUMP_INVENTORY_TOP.get(plugin.getConfigProvider().getMaxScoresPerJump()));
    }

    private final Jump jump;

    public JumpInventory(JumpPlugin plugin, Player player, Jump jump) {
        this(plugin, player, jump, null);
    }

    public JumpInventory(JumpPlugin plugin, Player player, Jump jump, Runnable backAction) {
        super(plugin, player, backAction);
        this.jump = jump;

        GLASS_PANE.getItemMeta();

        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName(Text.JUMP_INVENTORY_SELF.get());
        skull.setItemMeta(meta);


        this.inventory = Bukkit.createInventory(player, INVENTORY_SIZE, Text.JUMP_INVENTORY.get(jump.getName()));
        panePos.forEach(i -> this.inventory.setItem(i, GLASS_PANE));
        this.inventory.setItem(TOP_POS, TOP);
        this.inventory.setItem(SKULL_POS, skull);
        this.inventory.setItem(TP_POS, TP);

        super.addClickableItem(TP, this::onTp);
        super.addClickableItem(TOP, this::onTop);
        // get skull from inventory to grab updated item meta
        super.addClickableItem(this.inventory.getItem(SKULL_POS), this::onPlayerBestScores);

        // if an inventory can handle a back arrow
        if (backAction != null) {
            this.inventory.setItem(49, back);
            super.addClickableItem(back, super::onBack);
        }

        player.openInventory(this.inventory);
    }

    public void onTop(Event event) {
        new BestScoresInventory(this.plugin, this.player, this.jump,
                () -> new JumpInventory(this.plugin, this.player, this.jump, super.backAction));
    }

    public void onTp(Event event) {
        this.jump.getSpawn().ifPresent(this.player::teleport);
    }

    public void onPlayerBestScores(Event event) {
        List<Long> scores = this.plugin.getPlayerManager().getScores(this.player, this.jump);
        BookMeta meta = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
        String collect = Stream.iterate(0, i -> i + 1)
                .limit(scores.size())
                .map(i -> new ImmutablePair<>(i + 1, new TimeScore(scores.get(i))))
                .map(pair -> Text.BEST_SCORES_BOOK_LINE.get(
                        pair.left,
                        pair.right.getMinutes(),
                        pair.right.getSeconds(),
                        pair.right.getMillis()
                ))
                .collect(Collectors.joining("\n"));
        meta.setTitle("Best scores");
        meta.setAuthor("Nowtryz");
        meta.setPages(Text.BEST_SCORES_HEADER.get(
                this.player.getName(),
                this.plugin.getConfigProvider().getMaxScoresPerPlayer()
        ) + "\n\n" + collect);

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        book.setItemMeta(meta);

        int slot = this.player.getInventory().getHeldItemSlot();
        ItemStack old = this.player.getInventory().getItem(slot);
        this.player.getInventory().setItem(slot, book);

        try {
            PacketContainer pc = ProtocolLibrary.getProtocolManager().
                    createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
            pc.getModifier().writeDefaults();
            ByteBuf bf = Unpooled.buffer(256);
            bf.setByte(0, (byte)0); // main hand
            bf.writerIndex(1);
            pc.getStrings().write(0, "MC|BOpen");
            pc.getModifier().write(1, MinecraftReflection.getPacketDataSerializer(bf));
            ProtocolLibrary.getProtocolManager().sendServerPacket(this.player, pc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.player.getInventory().setItem(slot, old);
    }
}
