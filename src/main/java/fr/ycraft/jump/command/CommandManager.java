package fr.ycraft.jump.command;

import fr.ycraft.jump.command.annotations.Command;
import fr.ycraft.jump.command.annotations.Completer;
import fr.ycraft.jump.command.contexts.CompletionContext;
import fr.ycraft.jump.command.execution.Executor;
import fr.ycraft.jump.command.execution.MethodCompleter;
import fr.ycraft.jump.command.execution.MethodExecutor;
import fr.ycraft.jump.command.graph.CommandAdapter;
import fr.ycraft.jump.command.graph.CommandForest;
import fr.ycraft.jump.command.graph.CommandRoot;
import net.nowtryz.mcutils.command.CommandResult;
import net.nowtryz.mcutils.injection.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Singleton
public class CommandManager {
    private final MethodCompleter.Factory completerFactory;
    private final MethodExecutor.Factory executorFactory;
    private final CommandForest forest;
    private final CommandMap commandMap;
    private final Plugin plugin;
    private final Logger logger;

    // TODO context providers
    // TODO help command

    @Inject
    public CommandManager(
            Plugin plugin,
            CommandForest forest,
            MethodExecutor.Factory executorFactory,
            MethodCompleter.Factory completerFactory,
            @PluginLogger Logger logger) {
        this.forest = forest;
        this.logger = logger;
        this.plugin = plugin;
        this.executorFactory = executorFactory;
        this.completerFactory = completerFactory;

        try {
            Server server = Bukkit.getServer();
            Method mapGetter = server.getClass().getMethod("getCommandMap");
            this.commandMap = (CommandMap) mapGetter.invoke(server);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot get the command map from bukkit");
        }
    }

    /*
     * Registrations
     */

    /**
     * Collect all commands present in the given package
     * @param packageId the package name to check (e.g. com.group.project or com.group.project.commands)
     */
    public void collect(String packageId) {
        Reflections reflections = new Reflections(packageId, new MethodAnnotationsScanner());

        reflections.getMethodsAnnotatedWith(Command.class)
                .stream()
                .filter(method -> !method.isAnnotationPresent(Completer.class))
                .forEach(this::registerCommand);

        reflections.getMethodsAnnotatedWith(Completer.class)
                .stream()
                .filter(method -> !method.isAnnotationPresent(Command.class))
                .forEach(this::registerCompleter);
    }

    /**
     * Collect all commands present in the given class
     * @param clazz the class to check
     */
    public void collect(Class<?> clazz) {
        Arrays.stream(clazz.getDeclaredMethods())
                .filter(this::isCapable)
                .forEach(this::register);
    }

    private void register(Method method) {
        if (method.isAnnotationPresent(Command.class))
            if (!method.isAnnotationPresent(Completer.class)) this.registerCommand(method);
        else if (method.isAnnotationPresent(Completer.class))
            this.registerCompleter(method);
    }

    /*
     * Checks
     */

    private boolean isCapable(Method method) {
        return method.isAnnotationPresent(Command.class) && !method.isAnnotationPresent(Completer.class)
                || !method.isAnnotationPresent(Command.class) && method.isAnnotationPresent(Completer.class);
    }

    private void registerCommand(Method method) {
        MethodExecutor executor = this.executorFactory.create(method);

        if (!method.getReturnType().equals(CommandResult.class)) {
            this.logger.warning(String.format(
                    "%s is not a valid command, expected a %s return type but got %s",
                    method,
                    CommandResult.class.getSimpleName(),
                    method.getReturnType().getSimpleName()
            ));
        }

        else if (executor.getCommand().isEmpty()) this.logger.warning(String.format(
                "%s is not a valid command, given command line is empty",
                executor.methodID())
        );

        else this.forest.registerCommand(executor);
    }

    private void registerCompleter(Method method) {
        MethodCompleter completer = this.completerFactory.create(method);
        // checks
        if (!method.getReturnType().isAssignableFrom(List.class)) {
            this.logger.warning(String.format(
                    "%s is not a valid completer, expected a List<String> return type but got %s",
                    method.getDeclaringClass().getName() + "." + method.getName(),
                    method.getReturnType().getSimpleName()
            ));
        }

        else if (method.getParameterCount() != 1 || method.getParameterTypes()[0].isAssignableFrom(CompletionContext.class)) {
            this.logger.warning(String.format(
                    "%s is not a valid completer, expected one parameter of type CompletionContext",
                    method.getDeclaringClass().getName() + "." + method.getName()
            ));
        }

        else if (completer.getCommand().isEmpty()) this.logger.warning(String.format(
                "%s is not a valid completer, given command line is empty",
                method.getDeclaringClass().getName() + "." + method.getName()
        ));

        else this.forest.registerCompleter(completer);
    }

    /*
     * API
     */

    public void registerCommand(Executor executor) {
        this.forest.registerCommand(executor);
    }

    public void setResultHandler(ResultHandler handler) {
        this.forest.values().forEach(tree -> tree.getCommand().setHandler(handler));
    }

    /**
     * Register commands to Bukkit's {@link CommandMap}
     */
    public void registerCommands() {
        this.forest.values().forEach(this::register);
    }

    private void register(CommandRoot node) {
        org.bukkit.command.Command command = this.commandMap.getCommand(this.plugin.getName() + ':' + node.getKey());

        // update if a command is already present for the node
        // Will only be used if the use a fake reload in some ways
        if ((command instanceof CommandAdapter)) {
            CommandAdapter adapter = (CommandAdapter) command;

            // if the adapter references the same command node
            if (adapter.getNode() == node) return;
            // if the adapter is mapped to the same command but on a different node
            if (node.getKey().equals(adapter.getNode().getKey())) {
                adapter.setNode(node);
                node.setCommand(adapter);
                return;
            }
        } else if (command instanceof PluginCommand) {
            PluginCommand pluginCommand = (PluginCommand) command;

            // if the command was present in the plugin.yml and already registered by bukkit, we override executor and
            // tab completer to use the node implementations
            if (pluginCommand.getPlugin() == this.plugin) {
                CommandAdapter adapter = node.getCommand();
                pluginCommand.setExecutor((sender, cmd, label, args) -> adapter.execute(sender, label, args));
                pluginCommand.setTabCompleter((sender, cmd, label, args) -> adapter.tabComplete(sender, label, args));
                return;
            }
        }

        this.commandMap.register(this.plugin.getName(), node.getCommand());
    }

    public void printGraph() {
        Arrays.stream(this.forest.toStringGraph().split("\n")).forEach(this.logger::info);
    }
}
