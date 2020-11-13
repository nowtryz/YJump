package fr.ycraft.jump.command;

import fr.ycraft.jump.command.contexts.Context;
import lombok.Getter;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;

public enum SenderType {
    /**
     * Matches any console, local or remotes
     * @see org.bukkit.command.ConsoleCommandSender
     * @see org.bukkit.command.RemoteConsoleCommandSender
     */
    CONSOLE(ConsoleCommandSender.class, RemoteConsoleCommandSender.class),

    /**
     * Matches the local console
     * @see org.bukkit.command.ConsoleCommandSender
     */
    LOCAL_CONSOLE(ConsoleCommandSender.class),

    /**
     * Matches players
     * @see org.bukkit.entity.Player
     */
    PLAYER(Player.class),

    /**
     * Matches entities including players
     * @see org.bukkit.entity.Creature
     * @see org.bukkit.entity.Player
     */
    ENTITY(Creature.class, Player.class),

    /**
     * Matches only creatures (Mobs)
     * @see org.bukkit.entity.Creature
     */
    CREATURE(Creature.class),

    /**
     * Matches command blocks
     * @see BlockCommandSender
     */
    COMMAND_BLOCK(BlockCommandSender .class),

    /**
     * Matches any sender
     */
    ANY(CommandSender.class);

    @Getter
    private final Class<? extends CommandSender>[] acceptableSenders;

    @SafeVarargs
    SenderType(Class<? extends CommandSender>... acceptableSenders) {
        this.acceptableSenders = acceptableSenders;
    }

    /**
     * Check if the command sender is accepted by this sender type
     * @param sender the sender to check
     * @return true is the sender is acceptable
     */
    public boolean check(CommandSender sender) {
        for (Class<? extends CommandSender> acceptableSender : this.acceptableSenders) {
            if (acceptableSender.isAssignableFrom(sender.getClass())) return true;
        }

        return false;
    }

    /**
     * Check if the command sender is accepted by this sender type
     * @param context to check the sender from
     * @return true is the sender is acceptable
     */
    public boolean check(Context context) {
        return this.check(context.getSender());
    }
}
