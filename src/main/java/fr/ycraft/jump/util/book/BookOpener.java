package fr.ycraft.jump.util.book;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface BookOpener {
    void openBook(Player player, ItemStack book);
}
