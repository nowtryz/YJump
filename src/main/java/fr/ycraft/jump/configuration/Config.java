package fr.ycraft.jump.configuration;

import com.google.inject.Inject;
import net.nowtryz.mcutils.injection.DefaultConfig;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.Configuration;

import javax.inject.Singleton;

@Getter
@Singleton
public class Config {
    private final Object[] configurations = new Object[Key.values().size()];

    @Inject
    public Config(@DefaultConfig Configuration config) {
        for (Key<?> key : Key.values()) {
            this.configurations[key.ordinal()] = key.get(config);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(@NonNull Key<T> key) {
        if (key.ordinal() == Key.UNKNOWN_KEY) throw new IllegalArgumentException("Unknown key");
        return (T) this.configurations[key.ordinal()];
    }
}
