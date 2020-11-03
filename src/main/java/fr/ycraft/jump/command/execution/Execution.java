package fr.ycraft.jump.command.execution;

import lombok.Value;

public interface Execution {
    String getCommand();

    @Value
    class GenericArg {
        String arg;
        int index;
    }
}
