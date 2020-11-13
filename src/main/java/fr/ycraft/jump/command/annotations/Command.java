package fr.ycraft.jump.command.annotations;

import fr.ycraft.jump.command.SenderType;

import java.lang.annotation.*;

/**
 * Annotates a method as a command that can be collected by the {@link fr.ycraft.jump.command.CommandManager manager}.
 *
 * <p>This method will be called whenever a command sent to the server matche the command given to the annotation. The
 * command string must use chevrons to specify generic arguments, i.e. string argument that can be pass to the executor
 * (more info {@link Command#value() here}. See the doc of each properties to see how to use them.
 *
 * <br><p>This method can either or not be static. if static, it will be called independently. If the method is not static,
 * The executor will create an instance of the enclosing class using Guice's {@link com.google.inject.Injector Injector}.
 * Which means any command present in the same class will have a different instance so state can be share between
 * invocations of the same method but not with other commands. To fix this if it is an issue, you can add a
 * {@link com.google.inject.Singleton singleton} annotation on top of the class to force all executors to use the same
 * instance.
 *
 * <p>This method can use as many argument as needed following the rules bellow:
 * <ul>
 *     <li>To access a generic argument, use the {@link Arg Arg()} annotation.</li>
 *     <li>To access the sender (any class extending {@link org.bukkit.command.CommandSender}) simply specify the class
 *     needed.</li>
 *     <li>To access other context information (command line, command label, or other context objects provided by your
 *     application), use the {@link Context} annotation.</li>
 * </ul>
 *
 * <p>Simple example:
 * <code><pre>
 *      &#064;Command("command with &lt;argument&gt; and sub commands")
 * </pre></code>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {
    /**
     * The command string with generic parameters.
     * A generic parameter is described as a name encapsulated un chevrons (<code>&lt;name&gt;</code>).
     */
    String value();

    /**
     * This is the the type of senders targeted by this command.
     * @see SenderType
     */
    SenderType type() default SenderType.ANY;

    String description() default "";
    String usage() default "";

    /**
     * The permission the sender must hold to be able to perform this command
     * @see org.bukkit.permissions.Permission
     */
    String permission() default "";

    /**
     * Informs the execution system if this command can be executed asynchronously.
     * The execution process use by mc-utils is often asynchronous by default nut as many plugins
     * do not support well asynchronous commands, the defaults for commands is <code>sync</code>
     * and will run on main thread.
     *
     * <p>As a side-effect, commands synchronized with main thread may run a tick after the command
     * is sent to the server as the implementation would have take a bit of time to perform pre-execution
     * operations.
     *
     * <p>Be aware that the executor provide by mc-utils' {@link net.nowtryz.mcutils.injection.BukkitModule
     * Bukkit module} the Bukkit's {@link org.bukkit.scheduler.BukkitScheduler scheduler} witch means that awaiting
     * asynchronous code running through mc-utils' default executor will crash the server! Please specify that the
     * command is async in this cases.
     */
    boolean async() default false;
}
