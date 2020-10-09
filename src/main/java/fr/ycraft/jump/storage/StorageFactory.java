package fr.ycraft.jump.storage;

import com.google.inject.Injector;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.injection.PluginLogger;
import fr.ycraft.jump.storage.implementations.StorageImplementation;
import lombok.experimental.FieldDefaults;

import javax.inject.Inject;
import java.util.logging.Logger;

@FieldDefaults(makeFinal = true)
public class StorageFactory {
    Logger logger;
    Config config;
    Injector injector;

    @Inject
    public StorageFactory(@PluginLogger Logger logger, Config config, Injector injector) {
        this.logger = logger;
        this.config = config;
        this.injector = injector;
    }

    public StorageImplementation getImplementation() {
        try {
            StorageType storageType = this.config.get(Key.STORAGE_TYPE);
            this.logger.info(String.format("Initializing storage (%s)", storageType.getName()));
            StorageImplementation implementation = injector.getInstance(storageType.getImplementation());
            implementation.init();
            return implementation;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable initialize storage", exception);
        }
    }
}
