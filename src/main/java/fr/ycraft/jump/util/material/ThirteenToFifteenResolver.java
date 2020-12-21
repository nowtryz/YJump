package fr.ycraft.jump.util.material;

import org.bukkit.Material;

public class ThirteenToFifteenResolver implements MaterialResolver {
    @Override
    public Material[] getPlates() {
        return new Material[]{
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE, // Gold
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE, // Iron
            Material.STONE_PRESSURE_PLATE,
            Material.ACACIA_PRESSURE_PLATE,
            Material.BIRCH_PRESSURE_PLATE,
            Material.DARK_OAK_PRESSURE_PLATE,
            Material.JUNGLE_PRESSURE_PLATE,
            Material.OAK_PRESSURE_PLATE,
            Material.SPRUCE_PRESSURE_PLATE,
        };
    }

    @Override
    public Material getDefaultPlate() {
        return Material.LIGHT_WEIGHTED_PRESSURE_PLATE;
    }

    @Override
    public Material getEndIcon() {
        return Material.END_STONE;
    }
}
