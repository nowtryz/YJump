package fr.ycraft.jump.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.storage.StorageFactory;
import fr.ycraft.jump.storage.implementations.StorageImplementation;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
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
        return runnable -> {
            if (this.plugin.isReady()) Bukkit.getScheduler().runTaskAsynchronously(this.plugin, runnable);
            else ForkJoinPool.commonPool().execute(runnable);
        };
    }

    @Provides
    @Singleton
    StorageImplementation provideStorage(StorageFactory storageFactory) {
        return storageFactory.getImplementation();
    }
}
