package fr.ycraft.jump.command.annotations;

import fr.ycraft.jump.command.Provider;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Providers.class)
public @interface Provides {
    String target();
    Class<? extends Provider<?>> provider();
}
