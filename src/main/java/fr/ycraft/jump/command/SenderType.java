package fr.ycraft.jump.command;

import lombok.Getter;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;

public enum SenderType {
    /**
     * @see org.bukkit.command.ConsoleCommandSender
     * @see org.bukkit.command.RemoteConsoleCommandSender
     */
    CONSOLE(ConsoleCommandSender.class, RemoteConsoleCommandSender.class),

    /**
     * @see org.bukkit.command.ConsoleCommandSender
     */
    LOCAL_CONSOLE(ConsoleCommandSender.class),

    /**
     * @see org.bukkit.entity.Player
     */
    PLAYER(Player.class),

    /**
     * @see org.bukkit.entity.Creature
     * @see org.bukkit.entity.Player
     */
    ENTITY(Creature.class, Player.class),

    /**
     * @see org.bukkit.entity.Creature
     */
    CREATURE(Creature.class),

    /**
     * @see BlockCommandSender
     */
    COMMAND_BLOCK(BlockCommandSender .class);

    @Getter
    private final Class<? extends CommandSender>[] acceptableSenders;

    @SafeVarargs
    SenderType(Class<? extends CommandSender>... acceptableSenders) {
        this.acceptableSenders = acceptableSenders;
    }
}
