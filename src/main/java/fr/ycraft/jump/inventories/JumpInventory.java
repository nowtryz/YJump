package fr.ycraft.jump.inventories;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.inject.assistedinject.Assisted;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.commands.Perm;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.JumpPlayer;
import fr.ycraft.jump.entity.TimeScore;
import net.nowtryz.mcutils.injection.Nullable;
import net.nowtryz.mcutils.templating.Pattern;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.nowtryz.mcutils.api.Gui;
import net.nowtryz.mcutils.builders.ItemBuilder;
import net.nowtryz.mcutils.inventory.AbstractGui;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This gui show information about the selected jump: best scores, a tp button and a settings button
 */
public class JumpInventory  extends AbstractGui<JumpPlugin> {
    private final Jump jump;
    private final JumpPlayer jumpPlayer;
    private final InfoAdminInventory.Factory infoFactory;
    private final BestScoresInventory.Factory bestScoresFactory;

    @Inject
    JumpInventory(JumpPlugin plugin,
                  Config config,
                  InfoAdminInventory.Factory infoFactory,
                  BestScoresInventory.Factory bestScoresFactory,
                  @Named("INFO") Pattern pattern,
                  @Assisted Player player,
                  @Assisted JumpPlayer jumpPlayer,
                  @Assisted Jump jump,
                  @Assisted @Nullable Gui back) {
        super(plugin, player, back);
        this.bestScoresFactory = bestScoresFactory;
        this.infoFactory = infoFactory;
        this.jumpPlayer = jumpPlayer;
        this.jump = jump;

        ItemStack skull = ItemBuilder.skullForPlayer(player)
                .setDisplayName(Text.JUMP_INVENTORY_SELF.get())
                .build();

        pattern.builder(this)
                .name(Text.JUMP_INVENTORY.get(jump.getName()))
                // if an inventory can handle a back arrow
                .hookBack("back", b -> b.setDisplayName(Text.BACK))
                // other hooks
                .hookAction("player", this::onPlayerBestScores, skull)
                .hookAction("tp", this::onTp, b -> b.setDisplayName(Text.JUMP_INVENTORY_TP))
                .hookAction("top 10", this::onTop, b -> b.setDisplayName(Text.JUMP_INVENTORY_TOP, config.get(Key.MAX_SCORES_PER_JUMP)))
                // if admin
                .hookIf(Perm.EDIT.isHeldBy(player), builder ->
                        builder.hookAction("settings", this::onSettings, b -> b.setDisplayName(Text.JUMP_INVENTORY_SETTINGS))
                );
    }

    public void onTop(Event event) {
        this.bestScoresFactory.create(this.player, this.jump, this).open();
    }

    public void onSettings(Event event) {
        this.infoFactory.create(this.player, this.jump, this).open();
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
