package fr.ycraft.jump.injection;

import java.lang.annotation.*;

/**
 * This annotation allow Guice to give a null value to a parameter/field
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Nullable {
    String value() default "";
}
