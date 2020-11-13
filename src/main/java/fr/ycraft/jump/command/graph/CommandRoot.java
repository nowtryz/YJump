package fr.ycraft.jump.command.graph;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import fr.ycraft.jump.command.contexts.NodeSearchContext;
import fr.ycraft.jump.command.execution.Executor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.Plugin;

@Getter
@Setter
public class CommandRoot extends CommandNode {
    private final Plugin plugin;
    private CommandAdapter command;
    private String description = "";
    private String usage = "/";
    // usageMessage ?
    // permissionMessage ?

    @Inject
    public CommandRoot(Plugin plugin, @Assisted String key) {
        super(key);
        this.plugin = plugin;
        this.command = new CommandAdapter(this);
    }

    public Executor findExecutor(NodeSearchContext context) {
        return this.findExecutor(context, context.getQueue());
    }

    public CommandNode findCompleter(NodeSearchContext context) {
        return this.findCompleter(context, context.getQueue());
    }
}
