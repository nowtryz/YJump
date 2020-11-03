package fr.ycraft.jump.command.graph;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.ycraft.jump.command.annotations.Command;
import fr.ycraft.jump.command.annotations.Completer;
import fr.ycraft.jump.command.execution.MethodCompleter;
import fr.ycraft.jump.command.execution.MethodExecutor;
import net.nowtryz.mcutils.command.CommandResult;
import net.nowtryz.mcutils.injection.PluginLogger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class CommandForest extends HashMap<String, CommandTree> {
    private static final long serialVersionUID = 6366145974492100941L;
    private final CommandTree.Factory commandFactory;
    private final MethodExecutor.Factory executorFactory;
    private final Logger logger;

    @Inject
    public CommandForest(
            @PluginLogger Logger logger,
            CommandTree.Factory commandFactory,
            MethodExecutor.Factory executorFactory) {
        this.logger = logger;
        this.commandFactory = commandFactory;
        this.executorFactory = executorFactory;
    }

    public CommandTree get(String root) {
        return this.computeIfAbsent(root, this.commandFactory::create);
    }

    public void register(Method method) {
        if (method.isAnnotationPresent(Command.class)) this.registerCommand(method);
        else if (method.isAnnotationPresent(Completer.class)) this.registerCompleter(method);
    }

    private void registerCommand(Method method) {
        MethodExecutor executor = this.executorFactory.create(method);

        // checks
        if (!method.getReturnType().equals(CommandResult.class)) {
            this.logger.warning(String.format(
                    "%s is not a valid command, expected a %s return type but got %s",
                    method,
                    CommandResult.class.getSimpleName(),
                    method.getReturnType().getSimpleName()
            ));
            return;
        }
        if (executor.getCommand().isEmpty()) {
            this.logger.warning(String.format("%s is not a valid command, given command line is empty", method));
            return;
        }

        // Registration
        Queue<String> commandLine = new LinkedList<>(Arrays.asList(executor.getCommand().split(" ")));
        this.get(commandLine.remove()).registerCommand(commandLine, executor);
    }

    private void registerCompleter(Method method) {
        MethodCompleter completer = MethodCompleter.from(method);
        // checks
        if (!method.getReturnType().isAssignableFrom(List.class)) {
            this.logger.warning(String.format(
                    "%s is not a valid completer, expected a List<String> return type but got %s",
                    method,
                    method.getReturnType().getSimpleName()
            ));
            return;
        }
        if (completer.getCommand().isEmpty()) {
            this.logger.warning(String.format("%s is not a valid completer, given command line is empty", method));
            return;
        }

        // Registration
        Queue<String> commandLine = new LinkedList<>(Arrays.asList(completer.getCommand().split(" ")));
        this.get(commandLine.remove()).registerCompleter(commandLine, completer);
    }

    public String toStringGraph() {
        return "Forest {\n" +
                this.values()
                    .stream()
                    .map(node -> node.toStringGraph(1))
                    .collect(Collectors.joining("\n")) + "\n" +
                "}";
    }
}
