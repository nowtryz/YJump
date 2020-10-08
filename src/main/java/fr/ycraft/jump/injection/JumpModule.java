package fr.ycraft.jump.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Config;
import fr.ycraft.jump.storage.FlatFileStorage;
import fr.ycraft.jump.storage.MySQLStorage;
import fr.ycraft.jump.storage.StorageImplementation;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class JumpModule extends AbstractModule {
    private final JumpPlugin plugin;

    @Override
    protected void configure() {
        bind(Plugin.class).to(JumpPlugin.class);
        bind(JavaPlugin.class).to(JumpPlugin.class);
        bind(JumpPlugin.class).toInstance(this.plugin);
    }

    @Provides
    @PluginLogger
    Logger provideLogger() {
        return this.plugin.getLogger();
    }

    @Provides
    @DefaultConfig
    Configuration providePluginConfiguration() {
        return this.providePluginFileConfiguration();
    }

    @Provides
    @DefaultConfig
    FileConfiguration providePluginFileConfiguration() {
        return this.plugin.getConfig();
    }

    @Provides
    @Singleton
    @DataFolder
    File provideDataFolder() {
        return this.plugin.getDataFolder();
    }

    @Provides
    @BukkitExecutor
    Executor provideExecutor() {
        return runnable -> Bukkit.getScheduler().runTaskAsynchronously(this.plugin, runnable);
    }

    @Provides
    @Singleton
    StorageImplementation provideStorage(Config config, Injector injector, @PluginLogger Logger logger) {
        if (config.isDatabaseStorage()) {
            try {
                logger.info(String.format(
                        "Using %s database on %s:%d",
                        config.getDatabaseName(),
                        config.getDatabaseHost(),
                        config.getDatabasePort()
                ));
                MySQLStorage implementation = injector.getInstance(MySQLStorage.class);
                implementation.init();
                return implementation;
            } catch (SQLException exception) {
                logger.severe("Unable to connect to database: " + exception);
                logger.warning("Falling back to yaml files");
                logger.log(Level.SEVERE, "Stack trace:", exception);
                return this.makeFileStorage(injector, logger);
            }
        } else {
            return this.makeFileStorage(injector, logger);
        }
    }

    FlatFileStorage makeFileStorage(Injector injector, Logger logger) {
        FlatFileStorage implementation = injector.getInstance(FlatFileStorage.class);
        implementation.init();
        logger.info("Using YAML files");
        return implementation;
    }
}
