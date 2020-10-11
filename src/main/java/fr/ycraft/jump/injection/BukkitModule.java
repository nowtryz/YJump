package fr.ycraft.jump.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class BukkitModule<T extends JavaPlugin> extends AbstractModule {
    private final T plugin;
    private final Class<T> classOfT;

    @Override
    protected void configure() {
        bind(Plugin.class).to(classOfT);
        bind(JavaPlugin.class).to(classOfT);
        bind(classOfT).toInstance(this.plugin);
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
}
