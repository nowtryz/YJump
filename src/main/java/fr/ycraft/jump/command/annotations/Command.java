package fr.ycraft.jump.command.annotations;

import fr.ycraft.jump.command.SenderType;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    String value();
    SenderType type() default SenderType.PLAYER;
    String description() default "";
    String usage() default "";
    String permission() default "";
    boolean async() default false;
}
