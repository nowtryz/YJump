package fr.ycraft.jump.commands;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Utilities to information about the command
 */
public abstract class AbstractCommandImpl implements CommandImpl {
    protected final CommandSpec spec;
    protected Predicate<String[]> validator;

    public AbstractCommandImpl(CommandSpec spec) {
        this.spec = spec;
        this.validator = c -> c.length == 0;
    }

    public AbstractCommandImpl(CommandSpec spec, Predicate<String[]> validator) {
        this.spec = spec;
        this.validator = validator;
    }

    @Override
    public @NotNull String getKeyword() {
        return this.spec.label;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public boolean canAccept(String[] args) {
        return this.validator.test(args);
    }

    @Override
    public String getPermission() {
        return this.spec.permission.getPermission();
    }

    @Override
    public String getUsage() {
        return this.spec.getUsage();
    }

    @Override
    public boolean isPlayerCommand() {
        return false;
    }
}
