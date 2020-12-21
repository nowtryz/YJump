package fr.ycraft.jump.util.book;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class OneFifteenOpener implements BookOpener {
    @Override
    public void openBook(Player player, ItemStack book) {
        player.openBook(book);
    }
}
