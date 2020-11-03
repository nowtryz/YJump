package fr.ycraft.jump.command;

import fr.ycraft.jump.command.contexts.ExecutionContext;
import net.nowtryz.mcutils.command.CommandResult;

@FunctionalInterface
public interface ResultHandler {
    ResultHandler FALL_BACK = (context, result) -> {};

    void handle(ExecutionContext context, CommandResult result);
}
