package fr.ycraft.jump.command.annotations;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * String-based {@linkplain Qualifier qualifier} to identify argument
 *
 * <p>Example usage:
 *
 * <pre>
 *   public class TestCommand {
 *     &#064;Provides(target = "arg", provider = MyProvider.class)
 *     &#064;Command("command with &lt;arg&gt;")
 *     public void test(<b>@Arg("arg")</b> String arg, ... other arguments) {
 *         // actual command
 *     }
 *     ...
 *   }</pre>
 */
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface Arg {

    /** The name. */
    String value() default "";
}
