package fr.ycraft.jump.command.graph;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import fr.ycraft.jump.command.contexts.NodeSearchContext;
import fr.ycraft.jump.command.execution.Executor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.Plugin;

import java.util.List;

@Getter
@Setter
public class CommandTree extends CommandNode {
    private final CommandAdapter command;
    private final Plugin plugin;
    private String description = "";
    private String usage = "/";
    // usageMessage ?
    // permissionMessage ?

    public interface Factory {
        CommandTree create(String key);
    }

    @Inject
    public CommandTree(Plugin plugin, @Assisted String key) {
        super(key);
        this.plugin = plugin;
        this.command = new CommandAdapter(this);
    }

    public Executor findExecutor(NodeSearchContext context) {
        return this.findExecutor(context, context.getQueue());
    }

    public List<String> completeCommand(NodeSearchContext context) {
        return this.completeCommand(context, context.getQueue());
    }
}
