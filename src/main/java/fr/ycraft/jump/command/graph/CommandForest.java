package fr.ycraft.jump.command.graph;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.ycraft.jump.command.execution.Completer;
import fr.ycraft.jump.command.execution.Executor;
import net.nowtryz.mcutils.injection.PluginLogger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class CommandForest extends HashMap<String, CommandRoot> {
    private static final long serialVersionUID = 6366145974492100941L;
    private final CommandRootFactory commandFactory;
    private final Logger logger;

    @Inject
    public CommandForest(
            @PluginLogger Logger logger,
            CommandRootFactory commandFactory) {
        this.logger = logger;
        this.commandFactory = commandFactory;
    }

    public CommandRoot get(String root) {
        return this.computeIfAbsent(root, this.commandFactory::create);
    }

    public void registerCommand(Executor executor) {
        Queue<String> commandLine = new LinkedList<>(Arrays.asList(executor.getCommand().split(" ")));
        this.get(commandLine.remove()).registerCommand(commandLine, executor);
    }

    public void registerCompleter(Completer completer) {
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
