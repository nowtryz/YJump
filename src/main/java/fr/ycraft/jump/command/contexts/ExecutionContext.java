package fr.ycraft.jump.command.contexts;

import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import org.bukkit.Bukkit;

@Value
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ExecutionContext extends Context {
    @Default
    boolean isAsync = Bukkit.isPrimaryThread();
}
