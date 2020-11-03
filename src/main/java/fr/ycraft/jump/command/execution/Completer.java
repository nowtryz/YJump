package fr.ycraft.jump.command.execution;

import fr.ycraft.jump.command.contexts.NodeSearchContext;

import java.util.List;

public interface Completer extends Execution {
    List<String> complete(NodeSearchContext nodeSearchContext);
}
