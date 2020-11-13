package fr.ycraft.jump.commands;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.enums.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CommandImpl {
    /**
     * Executes the command
     * @param plugin the instance of the plugin that is running on the server
     * @param sender the command sender
     * @param args arguments of the command
     * @return true on success
     */
    boolean execute(JumpPlugin plugin, CommandSender sender, String[] args);

    /**
     * Return possible completion for the command
     * @param plugin the instance of the plugin that is running on the server
     * @param sender the command sender
     * @param args arguments of the command
     * @return true on success
     */
    List<String> tabComplete(JumpPlugin plugin, CommandSender sender, String[] args);

    /**
     * Get the keyword used by the command<br>
     *     Ex: /jump &lt;keyword&gt; [args...]
     * @return the keyword
     */
    @NotNull String getKeyword();

    /**
     * Is this commands intends and is able to run in an asynchronous thread
     * @return true if able
     */
    boolean isAsync();

    /**
     * Validates if arguments are applicable to this command
     * @param args the arguments to test
     * @return true if the command can accept de specified arguments
     */
    boolean canAccept(String[] args);

    /**
     * If this command intends to only be executed by a player
     * @return weather it's an player-only command
     */
    boolean isPlayerCommand();

    /**
     * Gets the permission required by this command to be executed
     * @return the permission string
     */
    String getPermission();

    /**
     * Get the usage string of the command
     * @return a translated message
     */
    String getUsage();

    /**
     * Processes the command and check arguments, permission and if this command can run asynchronously
     * @param plugin the instance of the plugin that is running on the server
     * @param sender the command sender
     * @param args arguments of the command
     * @return true on success
     */
    default boolean process(JumpPlugin plugin, CommandSender sender, String[] args) {
        // checks
        if (!this.canAccept(args)) Text.USAGE.send(sender, this.getUsage());
        else if (!sender.hasPermission(this.getPermission())) Text.NO_PERM.send(sender);

            // execution
        else if (!this.isAsync()) return this.execute(plugin, sender, args);
        else Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                long start = System.nanoTime();
                if (!this.execute(plugin, sender, args)) Text.USAGE.send(sender, this.getUsage());
                System.out.println(String.format("%07dns", System.nanoTime() - start));
            });

        return true;
    }
}
