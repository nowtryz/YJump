package fr.ycraft.jump.command;

import fr.ycraft.jump.command.annotations.Command;
import fr.ycraft.jump.command.annotations.Completer;
import fr.ycraft.jump.command.graph.CommandForest;
import net.nowtryz.mcutils.injection.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Logger;

@Singleton
public class CommandManager {
    private final Plugin plugin;
    private final CommandMap commandMap;
    private final CommandForest forest;
    private final Logger logger;

    @Inject
    public CommandManager(Plugin plugin, CommandForest forest, @PluginLogger Logger logger) {
        this.forest = forest;
        this.logger = logger;
        this.plugin = plugin;

        try {
            Server server = Bukkit.getServer();
            Method mapGetter = server.getClass().getMethod("getCommandMap");
            this.commandMap = (CommandMap) mapGetter.invoke(server);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot get the command map from bukkit");
        }
    }


    public void collect(String packageId) {
        Reflections reflections = new Reflections(packageId, new MethodAnnotationsScanner());

        reflections.getMethodsAnnotatedWith(Command.class)
                .stream()
                .filter(method -> !method.isAnnotationPresent(Completer.class))
                .forEach(this.forest::register);

        reflections.getMethodsAnnotatedWith(Completer.class)
                .stream()
                .filter(method -> !method.isAnnotationPresent(Command.class))
                .forEach(this.forest::register);
    }

    public void collect(Class<?> clazz) {
        Arrays.stream(clazz.getDeclaredMethods())
                .filter(this::isCapable)
                .forEach(this.forest::register);
    }

    private boolean isCapable(Method method) {
        return method.isAnnotationPresent(Command.class) && !method.isAnnotationPresent(Completer.class)
                || !method.isAnnotationPresent(Command.class) && method.isAnnotationPresent(Completer.class);
    }

    public void setResultHandler(ResultHandler handler) {
        this.forest.values().forEach(tree -> tree.getCommand().setHandler(handler));
    }

    /**
     * Register commands to Bukkit's {@link CommandMap}
     */
    public void registerCommands() {
        // TODO remove previous bindings
        this.forest.values().forEach(tree -> this.commandMap.register(this.plugin.getName(), tree.getCommand()));
    }

    public void printGraph() {
        Arrays.stream(this.forest.toStringGraph().split("\n")).forEach(this.logger::info);
    }
}
