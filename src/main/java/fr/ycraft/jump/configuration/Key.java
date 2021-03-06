package fr.ycraft.jump.configuration;

import com.google.common.collect.ImmutableList;
import fr.ycraft.jump.exceptions.ConfigurationInitializationException;
import fr.ycraft.jump.storage.StorageType;
import fr.ycraft.jump.util.material.MaterialResolver;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import static fr.ycraft.jump.configuration.KeyFactory.*;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class Key<T> implements Comparable<Key<?>> {
    // plates settings
    public static final Key<Boolean> DELETE_PLATES = booleanKey("plates.auto delete");
    public static final Key<Boolean> PLATES_PROTECTED = booleanKey("plates.protect");
    public static final Key<Material> START_MATERIAL = plateKey("plates.materials.start", MaterialResolver.getResolver().getDefaultPlate());
    public static final Key<Material> END_MATERIAL = plateKey("plates.materials.end", MaterialResolver.getResolver().getDefaultPlate());
    public static final Key<Material> CHECKPOINT_MATERIAL = plateKey("plates.materials.checkpoint", MaterialResolver.getResolver().getDefaultPlate());
    // game settings
    public static final Key<TitleSettings> START_TITLE = titleSettingsKey("game.title.start", false);
    public static final Key<TitleSettings> RESET_TITLE = titleSettingsKey("game.title.reset", false);
    public static final Key<TitleSettings> CHECKPOINT_TITLE = titleSettingsKey("game.title.checkpoint", false);
    public static final Key<TitleSettings> END_TITLE = titleSettingsKey("game.title.end", true);
    public static final Key<Boolean> ACTION_BAR_ENABLED = booleanKey("game.action bar", false);
    public static final Key<Boolean> SIDEBAR_ENABLED = booleanKey("game.sidebar", true);
    public static final Key<Boolean> BOSS_BAR_ENABLED = booleanKey("game.boss bar.enabled", true);
    public static final Key<BarColor> BOSS_BAR_COLOR = barColorKey("game.boss bar.color", BarColor.GREEN);
    public static final Key<Long> RESET_TIME = longKey("game.reset time");
    public static final Key<Integer> MAX_FALL_DISTANCE = intKey("game.max fall distance");
    public static final Key<Boolean> INTELLIGENT_FALL_DISTANCE = booleanKey("game.intelligent fall distance", true);
    public static final Key<Boolean> DISABLE_COLLISIONS = booleanKey("game.disable collisions", false);
    public static final Key<Boolean> RESET_ENCHANTS = booleanKey("game.reset enchants");
    public static final Key<List<String>> ALLOWED_COMMANDS = stringListKey("game.allowed commands");
    // editor settings
    public static final Key<Boolean> CREATIVE_EDITOR = booleanKey("editor.creative");
    // best scores settings
    public static final Key<Integer> MAX_SCORES_PER_JUMP = intKey("best scores.per jump");
    public static final Key<Integer> MAX_SCORES_PER_PLAYER = intKey("best scores.per player");
    // parkour settings
    public static final Key<Integer> DESCRIPTION_WRAP_LENGTH = intKey("parkour.description wrap length");
    public static final Key<Material> DEFAULT_JUMP_ICON = materialKey("parkour.default icon", Material.SLIME_BLOCK);
    // storage settings
    public static final Key<StorageType> STORAGE_TYPE = key(c -> StorageType.parse(c.getString("storage.implementation")));
    public static final Key<String> DATABASE_HOST = stringKey("storage.database.host", null);
    public static final Key<Integer> DATABASE_PORT = intKey("storage.database.port", 3308);
    public static final Key<String> DATABASE_NAME = stringKey("storage.database.name", null);
    public static final Key<String> DATABASE_USER = stringKey("storage.database.user", null);
    public static final Key<String> DATABASE_PASSWORD = stringKey("storage.database.password", null);
    // other settings
    public static final Key<Locale> LOCALE = key(c -> Optional.ofNullable(c.getString("locale"))
            .map(Locale::new)
            .orElseGet(Locale::getDefault));

    public static final int UNKNOWN_KEY = -1;
    private static final List<Key<?>> VALUES;

    static {
        // get a list of all keys
        List<Field> KEYS = Arrays.stream(Key.class.getFields())
                .filter(f -> Modifier.isStatic(f.getModifiers()))
                .filter(f -> Key.class.isAssignableFrom(f.getType()))
                .collect(ImmutableList.toImmutableList());

        VALUES = KEYS.stream().map(f -> {
            try {
                return (Key<?>) f.get(null);
            } catch (IllegalAccessException e) {
                throw new ConfigurationInitializationException(e);
            }
        }).collect(ImmutableList.toImmutableList());

        // set ordinal values and names
        for (int i = 0; i < KEYS.size(); i++) {
            Field field = KEYS.get(i);
            Key<?> value = VALUES.get(i);
            value.name = field.getName();
            value.ordinal = i;
        }
    }

    @Getter
    private int ordinal = UNKNOWN_KEY;
    @Getter
    private String name = "";
    private final Function<Configuration, T> extractor;

    public T get(Configuration configuration) {
        return extractor.apply(configuration);
    }

    @Override
    public final int compareTo(Key<?> other) {
        return this.ordinal - other.ordinal();
    }

    public final boolean equals(Object other) {
        return this==other;
    }

    public final String toString() {
        return name;
    }

    public final int hashCode() {
        return super.hashCode();
    }

    /*
     * Static methods
     */

    public static List<Key<?>> values() {
        return Key.VALUES;
    }
}
