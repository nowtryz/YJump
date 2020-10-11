package fr.ycraft.jump.commands;

import fr.ycraft.jump.JumpPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class PluginCommandExecutor extends AbstractCommandImpl implements TabExecutor {
    protected final JumpPlugin plugin;
    protected final Map<String, CommandImpl> commandsMap;
    protected final CommandImpl[] commands;

    public PluginCommandExecutor(@NotNull JumpPlugin plugin, CommandSpec spec, CommandImpl... commands) {
        super(spec);
        this.plugin = plugin;
        this.commands = commands;

        PluginCommand command = plugin.getCommand(spec.label);

        command.setExecutor(this);
        if (this.canComplete()) command.setTabCompleter(this);
        if (commands != null) this.commandsMap = Arrays
                .stream(commands)
                .collect(Collectors.toMap(cmd -> cmd.getKeyword().toLowerCase(), Function.identity()));
        else this.commandsMap = new HashMap<>();
    }

    public PluginCommandExecutor(@NotNull JumpPlugin plugin, CommandSpec spec, Predicate<String[]> validator, CommandImpl... commands) {
        super(spec, validator);
        this.plugin = plugin;
        this.commands = commands;

        if (commands != null) this.commandsMap = Arrays
                .stream(commands)
                .collect(Collectors.toMap(cmd -> cmd.getKeyword().toLowerCase(), Function.identity()));
        else this.commandsMap = new HashMap<>();
    }

    public void register() {
        PluginCommand command = plugin.getCommand(spec.label);

        command.setExecutor(this);
        if (this.canComplete()) command.setTabCompleter(this);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // if no sub commands, we run the default command
        if (args.length == 0 || this.commands == null) return this.process(plugin, sender, args);

        // grab sub command and execute it
        CommandImpl cmd = this.commandsMap.get(args[0].toLowerCase());
        if (cmd == null) return this.process(plugin, sender, args);
        else {
            // arguments to pass
            String[] subArgs = this.extractSubArgs(args);

            return cmd.process(plugin, sender, subArgs);
        }
    }

    /**
     * Create a new array and strip the first value
     * @param array the array to strip
     * @return an array with the first value stripped
     */
    private String[] extractSubArgs(String[] array) {
        int newLength = array.length - 1;
        String[] subArgs = new String[newLength];
        System.arraycopy(array, 1, subArgs, 0, newLength);
        return subArgs;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) return null;
        if (args.length == 1 && this.commands != null) {
            List<String> complete = Arrays.stream(this.commands)
                    .filter(c -> sender.hasPermission(c.getPermission()))
                    .map(CommandImpl::getKeyword)
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());

            Optional.ofNullable(this.tabComplete(plugin, sender, args)).ifPresent(complete::addAll);
            return complete;
        }

        return Optional.ofNullable(this.commandsMap.get(args[0].toLowerCase()))
                .map(cmd -> cmd.tabComplete(plugin, sender, this.extractSubArgs(args)))
                .orElseGet(() -> this.tabComplete(plugin, sender, args));
    }

    public boolean canComplete() { return false; }
}
