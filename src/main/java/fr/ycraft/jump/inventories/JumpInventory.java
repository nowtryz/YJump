package fr.ycraft.jump.inventories;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.inject.assistedinject.Assisted;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.commands.Perm;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.JumpPlayer;
import fr.ycraft.jump.entity.TimeScore;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.nowtryz.mcutils.ItemStackUtil;
import net.nowtryz.mcutils.api.Gui;
import net.nowtryz.mcutils.builders.ItemBuilder;
import net.nowtryz.mcutils.inventory.AbstractGui;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.ycraft.jump.util.ItemLibrary.BACK;
import static fr.ycraft.jump.util.ItemLibrary.WHITE_FILLER;

/**
 * This gui show information about the selected jump: best scores, a tp button and a settings button
 */
public class JumpInventory  extends AbstractGui<JumpPlugin> {
    private static final int INVENTORY_SIZE = 54;
    private static final List<Integer> panePos = Stream.iterate(0, i->i+1)
            .limit(INVENTORY_SIZE)
            .filter(i -> i/9 == 0 || i/9 == 5 || i%9 == 0 || i%9 == 8 || i == 10 || i == 16 || i == 37 || i == 43)
            .collect(Collectors.toList());

    private static final ItemStack TOP = new ItemStack(Material.WATCH);
    private static final ItemStack TP = new ItemStack(Material.COMPASS);
    private static final ItemStack SETTINGS = new ItemStack(Material.COMMAND);
    private static final int TOP_POS = 21, SKULL_POS = 23, TP_POS = 31, SETTINGS_POS = 38;

    public static void init(JumpPlugin plugin) {
        ItemStackUtil.setName(TP, Text.JUMP_INVENTORY_TP.get());
        ItemStackUtil.setName(SETTINGS, Text.JUMP_INVENTORY_SETTINGS.get());
        ItemStackUtil.setName(TOP, Text.JUMP_INVENTORY_TOP.get(plugin.getConfigProvider().get(Key.MAX_SCORES_PER_JUMP)));
    }

    private final Jump jump;
    private final JumpPlayer jumpPlayer;
    private final InfoInventory.Factory factory;

    @Inject
    JumpInventory(JumpPlugin plugin,
                         InfoInventory.Factory factory,
                         @Assisted Player player,
                         @Assisted JumpPlayer jumpPlayer,
                         @Assisted Jump jump,
                         @Assisted @Nullable Gui back) {
        super(plugin, player, back);
        this.factory = factory;
        this.jumpPlayer = jumpPlayer;
        this.jump = jump;

        ItemStack skull = ItemBuilder.skullForPlayer(player)
                .setDisplayName(Text.JUMP_INVENTORY_SELF.get())
                .build();


        Inventory inventory = Bukkit.createInventory(player, INVENTORY_SIZE, Text.JUMP_INVENTORY.get(jump.getName()));
        this.setInventory(inventory);
        panePos.forEach(i -> inventory.setItem(i, WHITE_FILLER));
        super.addClickableItem(TOP_POS, TOP, this::onTop);
        super.addClickableItem(SKULL_POS, skull, this::onPlayerBestScores);
        super.addClickableItem(TP_POS, TP, this::onTp);

        // if admin
        if (Perm.EDIT.isHeldBy(player)) super.addClickableItem(SETTINGS_POS, SETTINGS, this::onSettings);

        // if an inventory can handle a back arrow
        super.registerBackItem(BACK, 49);
    }

    public void onTop(Event event) {
        new BestScoresInventory(this.plugin, this.player, this.jump, this).open();
    }

    public void onSettings(Event event) {
        this.factory.create(this.player, this.jump, this).open();
    }

    public void onTp(Event event) {
        this.jump.getSpawn().ifPresent(this.player::teleport);
    }

    public void onPlayerBestScores(Event event) {
        List<TimeScore> scores = this.jumpPlayer.get(this.jump);
        BookMeta meta = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
        String collect = Stream.iterate(0, i -> i + 1)
                .limit(scores.size())
                .map(i -> new ImmutablePair<>(i + 1, scores.get(i)))
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
                this.plugin.getConfigProvider().get(Key.MAX_SCORES_PER_PLAYER)
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

    public interface Factory {
        JumpInventory create(Player player, JumpPlayer jumpPlayer, Jump jump, @Nullable Gui back);
    }
}
