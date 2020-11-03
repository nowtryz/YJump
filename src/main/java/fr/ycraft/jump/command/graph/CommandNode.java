package fr.ycraft.jump.command.graph;

import com.google.common.base.Strings;
import fr.ycraft.jump.command.contexts.NodeSearchContext;
import fr.ycraft.jump.command.exceptions.DuplicationException;
import fr.ycraft.jump.command.execution.Completer;
import fr.ycraft.jump.command.execution.Executor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class CommandNode {
    private static final Pattern GENERIC_MATCHER = Pattern.compile("<[^\\s]*>");

    @Getter(AccessLevel.NONE)
    private final Map<String, CommandNode> children = new HashMap<>();
    private final String key;

    private String permission = "";
    private CommandNode genericNode;
    private Executor executor;
    private Completer completer;

    @NotNull
    public CommandNode children(String key) {
        return this.children.computeIfAbsent(key, CommandNode::new);
    }

    private synchronized CommandNode getOrCreateGenericNode() {
        if (this.genericNode == null) this.genericNode = new CommandNode("<argument>");
        return this.genericNode;
    }

    public Executor execute(NodeSearchContext context) {
        if (this.executor == null) return null;
        return this.executor;
    }

    public List<String> complete(NodeSearchContext context) {
        return null;
    }

    public Executor findExecutor(NodeSearchContext context, Queue<String> remainingArgs) {
        if (remainingArgs.isEmpty()) return execute(context);

        String command = remainingArgs.remove();
        CommandNode child = this.children.getOrDefault(command, this.genericNode);

        if (child != null) return child.findExecutor(context, remainingArgs);
        else return null;
    }

    public List<String> completeCommand(NodeSearchContext context, Queue<String> remainingArgs) {
        if (remainingArgs.isEmpty()) return null;

        String command = remainingArgs.remove().toLowerCase();

        if (remainingArgs.size() == 1) {
            if (this.children.containsKey(command)) return null;
            else return this.children.values()
                    .stream()
                    .filter(n -> n.getKey().startsWith(command))
                    .filter(context::checkPermission)
                    .map(CommandNode::getKey)
                    .collect(Collectors.toList());
            // TODO add result from providers of generic node
        } else return Optional.ofNullable(this.children.get(command))
                .map(node -> node.completeCommand(context, remainingArgs))
                .orElse(null);
    }

    private void setCommand(Executor executor) {
        if (this.executor != null) throw new DuplicationException(this.executor, executor);
        this.executor = executor;
        // TODO analyse method
    }

    private void setCompleter(Completer completer) {
        if (this.completer != null) throw new DuplicationException(this.completer, completer);
        this.completer = completer;
        // TODO analyse method
    }

    public void registerCommand(Queue<String> commandLine, Executor executor) {
        if (commandLine.isEmpty()) {
            this.setCommand(executor);
            return;
        }

        String node = commandLine.remove();
        CommandNode children = GENERIC_MATCHER.matcher(node).matches() ? this.getOrCreateGenericNode() : this.children(node.toLowerCase());
        children.registerCommand(commandLine, executor);
    }

    public void registerCompleter(Queue<String> commandLine, Completer completer) {
        if (commandLine.isEmpty()) {
            this.setCompleter(completer);
            return;
        }

        String node = commandLine.remove();
        CommandNode children = GENERIC_MATCHER.matcher(node).matches() ? this.getOrCreateGenericNode() : this.children(node.toLowerCase());
        children.registerCompleter(commandLine, completer);
    }

    public String toStringGraph(int level) {
        int nextLevel = level + 1;
        String tabs = Strings.repeat("  ", level);
        StringBuilder builder = new StringBuilder().append(tabs).append(this.key).append(" {\n");
        if (this.executor != null) builder.append(tabs).append("  executor: ").append(executor).append("\n");
        if (this.completer != null) builder.append(tabs).append("  completer: ").append(completer).append("\n");
        if (this.genericNode != null) builder.append(this.genericNode.toStringGraph(nextLevel)).append("\n");
        if (!this.children.isEmpty()) for (CommandNode node : this.children.values()) {
            builder.append(node.toStringGraph(nextLevel)).append("\n");
        }

        return builder.append(tabs).append("}").toString();
    }
}
