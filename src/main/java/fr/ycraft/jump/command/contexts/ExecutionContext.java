package fr.ycraft.jump.command.contexts;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ExecutionContext extends Context {
    boolean isAsync;
}
