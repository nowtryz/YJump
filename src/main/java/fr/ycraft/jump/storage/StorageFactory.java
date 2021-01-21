package fr.ycraft.jump.storage;

import com.google.inject.Injector;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.storage.implementations.StorageImplementation;
import lombok.experimental.FieldDefaults;

import javax.inject.Inject;

@FieldDefaults(makeFinal = true)
public class StorageFactory {
    StorageType storageType;
    Injector injector;

    @Inject
    StorageFactory(Config config, Injector injector) {
        this.storageType = config.get(Key.STORAGE_TYPE);
        this.injector = injector;
    }

    public StorageImplementation getImplementation() {
        try {
            return injector.getInstance(storageType.getImplementation());
        } catch (Exception exception) {
            throw new IllegalStateException("Unable create storage", exception);
        }
    }
}
