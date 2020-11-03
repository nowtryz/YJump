package fr.ycraft.jump.command.graph;

import fr.ycraft.jump.command.ResultHandler;
import fr.ycraft.jump.command.contexts.ExecutionContext;
import fr.ycraft.jump.command.contexts.NodeSearchContext;
import fr.ycraft.jump.command.execution.Executor;
import lombok.NonNull;
import lombok.Setter;
import net.nowtryz.mcutils.command.CommandResult;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandAdapter extends Command {
    private final CommandTree node;

    @Setter
    private @NonNull ResultHandler handler = ResultHandler.FALL_BACK;

    protected CommandAdapter(CommandTree node) {
        super(node.getKey());
        this.node = node;
    }

    @Override
    public String getDescription() {
        return this.node.getDescription();
    }

    @Override
    public String getUsage() {
        return this.node.getUsage();
    }

    @Override
    public boolean execute(@NonNull CommandSender sender, @NonNull String commandLabel, @NonNull String[] args) {

        if (!this.node.getPlugin().isEnabled()) throw new CommandException(String.format(
                "Cannot execute command '%s' in plugin %s - plugin is disabled.",
                commandLabel,
                this.node.getPlugin().getDescription().getFullName()
        ));

        NodeSearchContext context = NodeSearchContext.builder()
                .sender(sender)
                .commandLabel(commandLabel)
                .args(args)
                .build();

        Bukkit.getScheduler().runTaskAsynchronously(this.node.getPlugin(), () -> {
            Executor executor = this.node.findExecutor(context);

            if (!executor.isAsync()) {
                Bukkit.getScheduler().runTask(this.node.getPlugin(), () -> this.execute(executor, context));
            } else this.execute(executor, context);
        });

        return true;
    }

    private boolean execute(Executor executor, NodeSearchContext searchContext) {
        ExecutionContext context = searchContext.executionBuilder()
                .isAsync(!Bukkit.isPrimaryThread())
                .build();

        if (executor == null) return this.handle(context, CommandResult.UNKNOWN);

        try {
            CommandResult result = executor.execute(context);
            return this.handle(context, result);
        } catch (Throwable throwable) {
            this.handle(context, CommandResult.INTERNAL_ERROR);
            throw new CommandException(String.format(
                    "Unhandled exception executing command '%s' in plugin %s",
                    context.getCommandLabel(),
                    this.node.getPlugin().getDescription().getFullName()
            ), throwable);
        }
    }

    private boolean handle(ExecutionContext context, @NonNull CommandResult result) {
        this.handler.handle(context, result);
        return result.isValid();
    }

    @Override
    public List<String> tabComplete(@NonNull CommandSender sender, @NonNull String alias, @NonNull String[] args) {
        try {
            return this.node.completeCommand(NodeSearchContext.builder()
                    .sender(sender)
                    .commandLabel(alias)
                    .args(args)
                    .build());
        } catch (Throwable throwable) {
            StringBuilder message = new StringBuilder()
                    .append("Unhandled exception during tab completion for command '/")
                    .append(alias)
                    .append(' ');

            for (String arg : args) message.append(arg).append(' ');

            message.deleteCharAt(message.length() - 1)
                    .append("' in plugin ")
                    .append(this.node.getPlugin().getDescription().getFullName());

            throw new CommandException(message.toString(), throwable);
        }
    }
}
