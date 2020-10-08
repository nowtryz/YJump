package fr.ycraft.jump.entity;

import com.google.inject.Inject;
import fr.ycraft.jump.injection.DefaultConfig;
import fr.ycraft.jump.injection.PluginLogger;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.util.List;
import java.util.logging.Logger;

@Getter
@Singleton
public class Config {
    private final Material startMaterial;
    private final Material endMaterial;
    private final Material checkpointMaterial;
    private final BarColor bossbarColor;
    private final List<String> allowedCommands;
    @Accessors(fluent = true)
    private final boolean doesDeletePlates, doesResetEnchants;
    private final boolean creativeEditor, databaseStorage, platesProtected;
    private final int maxFallDistance, maxScoresPerJump, maxScoresPerPlayer, descriptionWrapLength, databasePort;
    private final long resetTime;
    private final String databaseHost, databaseName, databaseUser, databasePassword;

    @Inject
    public Config(@DefaultConfig Configuration config, @PluginLogger Logger logger) {
        this.startMaterial = Config.extractMaterial(config.getString("materials.start"), logger);
        this.endMaterial = Config.extractMaterial(config.getString("materials.end"), logger);
        this.checkpointMaterial = Config.extractMaterial(config.getString("materials.checkpoint"), logger);
        this.bossbarColor = Config.extractBossbarColor(config.getString("game.bossbar"), logger);
        this.allowedCommands = config.getStringList("game.allowed commands");
        this.doesDeletePlates = config.getBoolean("plates.auto delete");
        this.platesProtected = config.getBoolean("plates.protect");
        this.maxFallDistance = config.getInt("game.max fall distance");
        this.maxScoresPerJump = config.getInt("best scores.per jump");
        this.maxScoresPerPlayer = config.getInt("best scores.per player");
        this.descriptionWrapLength = config.getInt("description wrap length");
        this.doesResetEnchants = config.getBoolean("game.reset enchants");
        this.creativeEditor = config.getBoolean("editor.creative");
        this.resetTime = config.getLong("game.reset time");

        this.databaseHost = config.getString("database.host", null);
        this.databaseName = config.getString("database.name", null);
        this.databaseUser = config.getString("database.user", null);
        this.databasePassword = config.getString("database.pass", null);
        this.databasePort = config.getInt("database.port", 3308);
        this.databaseStorage = this.databaseHost != null
                && this.databaseUser != null && this.databaseName != null
                && config.getBoolean("database.enabled", false);
    }

    private static Material extractMaterial(@NotNull String name, @NotNull Logger logger) {
        try {
            return Material.valueOf(String.format("%s_PLATE", name).toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.severe(String.format(
                    "%s is not a valid material, it must be either 'gold', 'iron', 'stone' or 'wood'", name));
            return Material.GOLD_PLATE;
        }
    }

    private static BarColor extractBossbarColor(@NotNull String name, @NotNull Logger logger) {
        try {
            return BarColor.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.severe(String.format("%s is not a valid bossbar color", name));
            return BarColor.GREEN;
        }
    }
}
