package fr.ycraft.jump.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import fr.ycraft.jump.factories.JumpEditorFactory;
import fr.ycraft.jump.factories.JumpGameFactory;
import fr.ycraft.jump.inventories.BestScoresInventory;
import fr.ycraft.jump.inventories.InfoAdminInventory;
import fr.ycraft.jump.inventories.JumpInventory;
import fr.ycraft.jump.inventories.ListInventory;
import fr.ycraft.jump.listeners.GameListener;
import fr.ycraft.jump.storage.StorageFactory;
import fr.ycraft.jump.storage.implementations.StorageImplementation;

public class JumpModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(JumpGameFactory.class));
        install(new FactoryModuleBuilder().build(JumpEditorFactory.class));
        install(new FactoryModuleBuilder().build(BestScoresInventory.Factory.class));
        install(new FactoryModuleBuilder().build(InfoAdminInventory.Factory.class));
        install(new FactoryModuleBuilder().build(JumpInventory.Factory.class));
        install(new FactoryModuleBuilder().build(ListInventory.Factory.class));
        install(new FactoryModuleBuilder().build(GameListener.Factory.class));
    }

    @Provides
    @Singleton
    StorageImplementation provideStorage(StorageFactory storageFactory) {
        return storageFactory.getImplementation();
    }
}
