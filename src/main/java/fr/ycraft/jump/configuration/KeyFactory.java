package fr.ycraft.jump.configuration;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.Configuration;

import java.util.List;
import java.util.function.Function;

public class KeyFactory {
    static <T> Key<T> key(Function<Configuration, T> extractor) {
        return new Key<>(extractor);
    }

    static Key<Boolean> booleanKey(String path, boolean def) {
        return key(configuration -> configuration.getBoolean(path, def));
    }

    static Key<Boolean> booleanKey(String path) {
        return key(configuration -> configuration.getBoolean(path));
    }

    static Key<Integer> intKey(String path) {
        return key(configuration -> configuration.getInt(path));
    }

    static Key<Integer> intKey(String path, int def) {
        return key(configuration -> configuration.getInt(path, def));
    }

    static Key<Long> longKey(String path) {
        return key(configuration -> configuration.getLong(path));
    }

    static Key<String> stringKey(String path, String def) {
        return key(configuration -> configuration.getString(path, def));
    }

    static Key<String> stringKey(String path) {
        return key(configuration -> configuration.getString(path));
    }

    static Key<List<String>> stringListKey(String path) {
        return key(configuration -> configuration.getStringList(path));
    }

    static Key<Material> plateKey(String path, Material def) {
        return key(configuration -> {
            try {
                return Material.matchMaterial(String.format("%s_PLATE", configuration.getString(path)));
            } catch (IllegalArgumentException ignored) {}
            return def;
        });
    }

    static Key<Material> materialKey(String path, Material def) {
        return key(configuration -> {
            Material material = Material.matchMaterial(configuration.getString(path));
            return material != null ? material : def;
        });
    }

    static Key<BarColor> barColorKey(String path, BarColor def) {
        return key(configuration -> {
            try {
                return BarColor.valueOf(configuration.getString(path, "").toUpperCase());
            } catch (IllegalArgumentException ignored) {}
            return def;
        });
    }
}
