package fr.ycraft.jump.util.book;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BelowOneFifteenOpener implements BookOpener {
    @Override
    public void openBook(Player player, ItemStack book) {
        int slot = player.getInventory().getHeldItemSlot();
        ItemStack old = player.getInventory().getItem(slot);
        player.getInventory().setItem(slot, book);

        try {
            PacketContainer pc = ProtocolLibrary.getProtocolManager().
                    createPacket(PacketType.Play.Server.CUSTOM_PAYLOAD);
            pc.getModifier().writeDefaults();
            ByteBuf bf = Unpooled.buffer(256);
            bf.setByte(0, (byte)0); // main hand
            bf.writerIndex(1);
            pc.getStrings().write(0, "MC|BOpen");
            pc.getModifier().write(1, MinecraftReflection.getPacketDataSerializer(bf));
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, pc);
        } catch (Exception e) {
            // TODO would be a great idea to handle this exception
            e.printStackTrace();
        }

        player.getInventory().setItem(slot, old);
    }
}
