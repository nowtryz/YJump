package fr.ycraft.jump.command.execution;

import fr.ycraft.jump.command.contexts.ExecutionContext;
import net.nowtryz.mcutils.command.CommandResult;


public interface Executor extends Execution {
    CommandResult execute(ExecutionContext context) throws Throwable;

    default boolean isAsync() {
        return false;
    }
}
