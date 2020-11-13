package fr.ycraft.jump.injection;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import fr.ycraft.jump.command.execution.MethodCompleter;
import fr.ycraft.jump.command.execution.MethodExecutor;
import fr.ycraft.jump.command.graph.CommandRootFactory;

public class CommandModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(CommandRootFactory.class));
        install(new FactoryModuleBuilder().build(MethodExecutor.Factory.class));
        install(new FactoryModuleBuilder().build(MethodCompleter.Factory.class));
    }
}
