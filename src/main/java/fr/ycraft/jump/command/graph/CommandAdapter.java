package fr.ycraft.jump.command.graph;

import fr.ycraft.jump.command.ResultHandler;
import fr.ycraft.jump.command.SenderType;
import fr.ycraft.jump.command.contexts.ExecutionContext;
import fr.ycraft.jump.command.contexts.NodeSearchContext;
import fr.ycraft.jump.command.execution.Executor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.nowtryz.mcutils.command.CommandResult;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CommandAdapter extends Command {
    @Setter @Getter
    private CommandRoot node;

    @Setter
    private @NonNull ResultHandler handler = ResultHandler.FALL_BACK;

    public CommandAdapter(CommandRoot node) {
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

        Executor executor = this.node.findExecutor(context);

        if (executor == null) {
            this.handle(context.execution().build(), CommandResult.UNKNOWN);
            return CommandResult.UNKNOWN.isValid();
        }

        if (executor.isAsync()) {
            Bukkit.getScheduler().runTaskAsynchronously(this.node.getPlugin(), () -> this.execute(executor, context));
            // As we cannot know the effective result, we prevent the default failure behavior
            return true;
        } else return this.execute(executor, context);
    }

    private boolean execute(@NotNull Executor executor, NodeSearchContext context) {
        ExecutionContext executionContext = context.execution().build();
        SenderType senderType = executor.getType();

        if (!context.checkPermission(executor)) {
            return this.handle(executionContext, CommandResult.MISSING_PERMISSION);
        }

        if(!senderType.check(context)) {
            if (senderType == SenderType.PLAYER) return this.handle(executionContext, CommandResult.NOT_A_PLAYER);
            else if (senderType == SenderType.CONSOLE) return this.handle(executionContext, CommandResult.NOT_A_CONSOLE);
            else return this.handle(executionContext, CommandResult.WRONG_TARGET);
        }

        try {
            CommandResult result = executor.execute(executionContext);
            return this.handle(executionContext, result);
        } catch (Throwable throwable) {
            this.handle(executionContext, CommandResult.INTERNAL_ERROR);
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
        NodeSearchContext context = NodeSearchContext.builder()
                .sender(sender)
                .commandLabel(alias)
                .args(args)
                .build();


        try {
            CommandNode completer = this.node.findCompleter(context);
            if (completer == null) return null;
            return completer.complete(context);
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

    @Override
    public String toString() {
        return getClass().getName() + '(' + node.getKey() + ", " + node.getPlugin() + ')';
    }
}
