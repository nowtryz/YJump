package fr.ycraft.jump.injection;

import fr.ycraft.jump.enums.Patterns;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Patterns-based {@linkplain Qualifier qualifier}.
 *
 * <p>Example usage:
 *
 * <pre>
 *   public class Car {
 *     &#064;Inject <b>@Patterned(Patterns.ANY)</b> Pattern pattern;
 *     ...
 *   }</pre>
 */
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface Patterned {

    /** The name. */
    Patterns value();
}
