package fr.ycraft.jump.entity;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.logging.Logger;

public class Config {
    private final Material startMaterial;
    private final Material endMaterial;
    private final Material checkpointMaterial;
    private final BarColor bossbarColor;
    private final List<String> allowedCommands;
    private final boolean deletePlates, protectPlates, resetEnchants, creativeEditor;
    private final int maxFallDistance, maxScoresPerJump, maxScoresPerPlayer, descriptionWrapLength;
    private final long resetTime;

    public Config(
            Material startMaterial,
            Material endMaterial,
            Material checkpointMaterial,
            BarColor bossbarColor,
            List<String> allowedCommands,
            boolean deletePlates,
            boolean protectPlates,
            int maxFallDistance,
            int maxScoresPerJump,
            int maxScoresPerPlayer,
            int descriptionWrapLength,
            boolean resetEnchants,
            boolean creativeEditor,
            long resetTime) {
        this.startMaterial = startMaterial;
        this.endMaterial = endMaterial;
        this.checkpointMaterial = checkpointMaterial;
        this.bossbarColor = bossbarColor;
        this.allowedCommands = allowedCommands;
        this.deletePlates = deletePlates;
        this.protectPlates = protectPlates;
        this.maxFallDistance = maxFallDistance;
        this.maxScoresPerJump = maxScoresPerJump;
        this.maxScoresPerPlayer = maxScoresPerPlayer;
        this.descriptionWrapLength = descriptionWrapLength;
        this.resetEnchants = resetEnchants;
        this.creativeEditor = creativeEditor;
        this.resetTime = resetTime;
    }

    public Material getStartMaterial() {
        return startMaterial;
    }

    public Material getEndMaterial() {
        return endMaterial;
    }

    public Material getCheckpointMaterial() {
        return checkpointMaterial;
    }

    public BarColor getBossbarColor() {
        return bossbarColor;
    }

    public List<String> getAllowedCommands() {
        return allowedCommands;
    }

    public boolean doesDeletePlates() {
        return this.deletePlates;
    }

    public boolean isPlatesProtected() {
        return this.protectPlates;
    }

    public int getMaxFallDistance() {
        return this.maxFallDistance;
    }

    public int getMaxScoresPerJump() {
        return maxScoresPerJump;
    }

    public int getMaxScoresPerPlayer() {
        return maxScoresPerPlayer;
    }

    public int getDescriptionWrapLength() {
        return descriptionWrapLength;
    }

    public boolean doesResetEnchants() {
        return this.resetEnchants;
    }

    public boolean isCreativeEditor() {
        return creativeEditor;
    }

    public long getResetTime() {
        return this.resetTime;
    }

    public static Config fromYAML(FileConfiguration config, Logger logger) {
        return new Config(
                Config.extractMaterial(config.getString("materials.start"), logger),
                Config.extractMaterial(config.getString("materials.end"), logger),
                Config.extractMaterial(config.getString("materials.checkpoint"), logger),
                Config.extractBossbarColor(config.getString("game.bossbar"), logger),
                config.getStringList("game.allowed commands"),
                config.getBoolean("plates.auto delete"),
                config.getBoolean("plates.protect"),
                config.getInt("game.max fall distance"),
                config.getInt("best scores.per jump"),
                config.getInt("best scores.per player"),
                config.getInt("description wrap length"),
                config.getBoolean("game.reset enchants"),
                config.getBoolean("editor.creative"),
                config.getLong("game.reset time")
        );
    }

    private static Material extractMaterial(@NotNull String name, @NotNull Logger logger) {
        try {
            return Material.valueOf(String.format("%s_PLATE", name).toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.severe(String.format(
                    "%s is not a valid material, it must be either 'gold', 'iron', 'stone' or 'wood'", name)
            );
            return Material.GOLD_PLATE;
        }
    }

    private static BarColor extractBossbarColor(@NotNull String name, @NotNull Logger logger) {
        try {

            return BarColor.valueOf(String.format("%s", name).toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.severe(String.format(
                    "%s is not a valid bossbar color", name)
            );
            return BarColor.GREEN;
        }
    }

    private static Material extractToolMaterial(@NotNull String name,  @NotNull Logger logger) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.severe(String.format(
                    "%s is not a valid material", name)
            );
            return Material.STICK;
        }
    }
}
