package fr.ycraft.jump.command.contexts;

import fr.ycraft.jump.command.contexts.ExecutionContext.ExecutionContextBuilder;
import fr.ycraft.jump.command.graph.CommandNode;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

@Value
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class NodeSearchContext extends Context {
    public Queue<String> getQueue() {
        return new LinkedList<>(Arrays.asList(this.args));
    }

    public boolean checkPermission(CommandNode node) {
        if (sender.isOp()) return true;

        String permission = node.getPermission();
        if (permission == null || permission.isEmpty()) return true;

        return sender.hasPermission(permission);
    }

    public ExecutionContextBuilder<?,?> executionBuilder() {
        return ExecutionContext.builder()
                .sender(sender)
                .commandLabel(commandLabel)
                .args(args);
    }
}
