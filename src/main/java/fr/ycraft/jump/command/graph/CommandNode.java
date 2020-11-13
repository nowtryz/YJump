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

@Getter
@RequiredArgsConstructor
public class CommandNode {
    private static final Pattern GENERIC_MATCHER = Pattern.compile("<[^\\s]*>");

    @Getter(AccessLevel.NONE)
    private final HashMap<String, CommandNode> children = new HashMap<>();
    private final String key;

    private GenericCommandNode genericNode;
    private Executor executor;

    @NotNull
    public CommandNode children(String key) {
        return this.children.computeIfAbsent(key, CommandNode::new);
    }

    private synchronized GenericCommandNode getOrCreateGenericNode() {
        if (this.genericNode == null) this.genericNode = new GenericCommandNode("<argument>");
        return this.genericNode;
    }

    Executor findExecutor(NodeSearchContext context, Queue<String> remainingArgs) {
        if (remainingArgs.isEmpty()) return this.executor;

        String command = remainingArgs.remove();
        CommandNode child = this.children.getOrDefault(command, this.genericNode);

        if (child != null) return child.findExecutor(context, remainingArgs);
        else return null;
    }

    CommandNode findCompleter(NodeSearchContext context, Queue<String> remainingArgs) {
        if (remainingArgs.isEmpty()) return null;

        String command = remainingArgs.remove().toLowerCase();

        return remainingArgs.isEmpty() ?  this :  Optional
                .ofNullable(this.children.getOrDefault(command, this.genericNode))
                .map(node -> node.findCompleter(context, remainingArgs))
                .orElse(null);
    }

    List<String> complete(NodeSearchContext context) {
        ArrayList<String> list = new ArrayList<>();
        this.children.values()
                .stream()
                .filter(n -> n.getKey().startsWith(context.getLastArgument()))
                .filter(context::checkPermission)
                .map(CommandNode::getKey)
                .forEach(list::add);

        Optional.ofNullable(this.genericNode)
                .map(node -> node.completeArgument(context))
                .ifPresent(list::addAll);

        return list;
    }

    void setCommand(Executor executor) {
        if (this.executor != null) throw new DuplicationException(this.executor, executor);
        this.executor = executor;
    }

    void registerCommand(Queue<String> commandLine, Executor executor) {
        if (commandLine.isEmpty()) {
            this.setCommand(executor);
            return;
        }

        String node = commandLine.remove();
        CommandNode children = GENERIC_MATCHER.matcher(node).matches() ? this.getOrCreateGenericNode() : this.children(node.toLowerCase());
        children.registerCommand(commandLine, executor);
    }

    void registerCompleter(Queue<String> commandLine, Completer completer) {
        if (commandLine.isEmpty()) throw new IllegalStateException("Cannot register a completer for an empty command");
        else if (commandLine.size() == 1) {
            this.getOrCreateGenericNode().setCompleter(completer);
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
        this.appendToStringGraph(tabs, builder);
        if (this.executor != null) builder.append(tabs).append("  executor: ").append(executor).append("\n");
        if (this.genericNode != null) builder.append(this.genericNode.toStringGraph(nextLevel)).append("\n");
        if (!this.children.isEmpty()) for (CommandNode node : this.children.values()) {
            builder.append(node.toStringGraph(nextLevel)).append("\n");
        }

        return builder.append(tabs).append("}").toString();
    }

    protected void appendToStringGraph(String tabs, StringBuilder builder) {}

    @Override
    public String toString() {
        return this.toStringGraph(0);
    }
}
