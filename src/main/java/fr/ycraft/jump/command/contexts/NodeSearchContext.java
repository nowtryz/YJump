package fr.ycraft.jump.command.contexts;

import fr.ycraft.jump.command.contexts.CompletionContext.CompletionContextBuilder;
import fr.ycraft.jump.command.contexts.ExecutionContext.ExecutionContextBuilder;
import fr.ycraft.jump.command.execution.Executor;
import fr.ycraft.jump.command.graph.CommandNode;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@Value
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class NodeSearchContext extends Context {
    public Queue<String> getQueue() {
        return new LinkedList<>(Arrays.asList(this.args));
    }

    public String getLastArgument() {
        if (this.args.length == 0) return null;
        return this.args[this.args.length - 1];
    }

    public boolean checkPermission(CommandNode node) {
        return Optional.ofNullable(node.getExecutor())
                .map(this::checkPermission)
                .orElse(true);
    }

    public boolean checkPermission(Executor executor) {
        if (sender.isOp()) return true;

        String permission = executor.getPermission();
        if (permission == null || permission.isEmpty()) return true;

        return sender.hasPermission(permission);
    }

    public ExecutionContextBuilder<?,?> execution() {
        return ExecutionContext.builder()
                .sender(sender)
                .commandLabel(commandLabel)
                .args(args);
    }


    public CompletionContextBuilder<?,?> completion() {
        return CompletionContext.builder()
                .sender(sender)
                .commandLabel(commandLabel)
                .args(args)
                .argument(this.getLastArgument());
    }
}
