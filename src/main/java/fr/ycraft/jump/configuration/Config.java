package fr.ycraft.jump.configuration;

import com.google.inject.Inject;
import fr.ycraft.jump.injection.DefaultConfig;
import fr.ycraft.jump.injection.PluginLogger;
import lombok.Getter;
import org.bukkit.configuration.Configuration;

import javax.inject.Singleton;
import java.util.logging.Logger;

@Getter
@Singleton
public class Config {
    private final Object[] configurations = new Object[Key.values().size()];

    @Inject
    public Config(@DefaultConfig Configuration config, @PluginLogger Logger logger) {
        for (Key<?> extractor : Key.values()) {
            this.configurations[extractor.ordinal()] = extractor.get(config);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Key<T> key) {
        return (T) this.configurations[key.ordinal()];
    }
}
