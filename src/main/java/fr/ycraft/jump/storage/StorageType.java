package fr.ycraft.jump.storage;

import fr.ycraft.jump.storage.implementations.FlatFileStorage;
import fr.ycraft.jump.storage.implementations.MySQLStorage;
import fr.ycraft.jump.storage.implementations.NitriteStorage;
import fr.ycraft.jump.storage.implementations.StorageImplementation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public enum StorageType {
    YAML(FlatFileStorage.class, "YAML flat files", "YAML", "YML", "flatfile", "flat", "file"),
    MYSQL(MySQLStorage.class, "MYSQL database", "mysql", "sql"),
    NITRITE(NitriteStorage.class, "Nitrite Database", "nitrite", "nitrite-database", "nitrite-java", "noo", "no2");

    public static final StorageType DEFAULT = NITRITE;

    String name;
    String[] keyWords;
    Class<? extends StorageImplementation> implementation;

    StorageType(Class<? extends StorageImplementation> implementation, String name, String... keyWords) {
        this.keyWords = Arrays.stream(keyWords).map(String::toLowerCase).toArray(String[]::new);
        this.implementation = implementation;
        this.name = name;
    }

    public static StorageType parse(String name) {
        for (StorageType storage : StorageType.values()) {
            for (String keyWord : storage.keyWords) {
                if (keyWord.equalsIgnoreCase(name)) return storage;
            }
        }

        return DEFAULT;
    }
}
