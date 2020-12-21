package fr.ycraft.jump.configuration;

import fr.ycraft.jump.util.material.MaterialResolver;
import net.nowtryz.mcutils.MCUtils;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Locale;
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

    private static final String PLATE_APPENDER = MCUtils.THIRTEEN_COMPATIBLE ? "%s_PRESSURE_PLATE" : "%s_PLATE";
    static Key<Material> plateKey(String path, Material def) {

        return key(configuration -> {
            try {
                String value = configuration.getString(path);

                if (value == null) return def;
                if (MCUtils.THIRTEEN_COMPATIBLE) {
                    switch (value.trim().toLowerCase()) {
                        case "iron": return MaterialResolver.ironPlate();
                        case "gold": return MaterialResolver.goldPlate();
                    }
                }

                return Material.matchMaterial(String.format(PLATE_APPENDER, value));
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

    static Key<TitleSettings> titleSettingsKey(String path, boolean enabledByDefault) {
        return titleSettingsKey(path, enabledByDefault, 10, 10, 60);
    }

    static Key<TitleSettings> titleSettingsKey(String path, boolean enabledByDefault, int defFadeIn, int defFadeOut, int defStay) {
        return key(configuration -> {
            ConfigurationSection section = configuration.getConfigurationSection(path);
            return TitleSettings.builder()
                    .enabled(section.getBoolean("enabled", enabledByDefault))
                    .fadeIn(section.getInt("fade in", defFadeIn))
                    .fadeOut(section.getInt("fade out", defFadeOut))
                    .stay(section.getInt("stay", defStay))
                    .build();
        });
    }
}
