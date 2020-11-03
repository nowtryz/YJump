package fr.ycraft.jump.command.annotations;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A qualifier that decorate context member.
 * Which is all the members contained in the execution context except the
 * CommandSender or its instance cast to child type
 */
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface Context {}
