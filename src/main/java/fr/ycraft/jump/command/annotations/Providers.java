package fr.ycraft.jump.command.annotations;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Providers {
    Provides[] value();
}
