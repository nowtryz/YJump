package fr.ycraft.jump.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import fr.ycraft.jump.factories.JumpEditorFactory;
import fr.ycraft.jump.factories.JumpGameFactory;
import fr.ycraft.jump.inventories.*;
import fr.ycraft.jump.listeners.GameListener;
import fr.ycraft.jump.storage.StorageFactory;
import fr.ycraft.jump.storage.implementations.StorageImplementation;
import fr.ycraft.jump.util.book.BelowOneFifteenOpener;
import fr.ycraft.jump.util.book.BookOpener;
import fr.ycraft.jump.util.book.OneFifteenOpener;
import fr.ycraft.jump.util.material.MaterialResolver;
import fr.ycraft.jump.util.material.SixteenResolver;
import fr.ycraft.jump.util.material.ThirteenToFifteenResolver;
import fr.ycraft.jump.util.material.TwelveResolver;
import net.nowtryz.mcutils.MCUtils;

public class JumpModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder().build(JumpGameFactory.class));
        install(new FactoryModuleBuilder().build(JumpEditorFactory.class));
        install(new FactoryModuleBuilder().build(FallDistanceInventory.Factory.class));
        install(new FactoryModuleBuilder().build(BestScoresInventory.Factory.class));
        install(new FactoryModuleBuilder().build(InfoAdminInventory.Factory.class));
        install(new FactoryModuleBuilder().build(JumpInventory.Factory.class));
        install(new FactoryModuleBuilder().build(ListInventory.Factory.class));
        install(new FactoryModuleBuilder().build(GameListener.Factory.class));

        // Book opener binding
        bind(BookOpener.class).to(MCUtils.FIFTEEN_COMPATIBLE ? OneFifteenOpener.class : BelowOneFifteenOpener.class);
    }

    @Provides
    MaterialResolver providesMaterialResolver() {
        if (MCUtils.SIXTEEN_COMPATIBLE) return new SixteenResolver();
        if (MCUtils.THIRTEEN_COMPATIBLE) return new ThirteenToFifteenResolver();
        return new TwelveResolver();
    }

    @Provides
    @Singleton
    StorageImplementation provideStorage(StorageFactory storageFactory) {
        return storageFactory.getImplementation();
    }
}
