package fr.ycraft.jump.factories;

import fr.ycraft.jump.JumpEditor;
import fr.ycraft.jump.JumpGame;
import fr.ycraft.jump.entity.Jump;
import org.bukkit.entity.Player;

public interface JumpGameFactory {
    JumpGame create(Jump jump, Player player);
}
