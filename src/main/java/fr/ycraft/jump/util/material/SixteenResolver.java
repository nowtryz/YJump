package fr.ycraft.jump.util.material;

import org.bukkit.Material;

public class SixteenResolver extends ThirteenToFifteenResolver implements MaterialResolver {
    @Override
    public Material[] getPlates() {
        return new Material[]{
                Material.LIGHT_WEIGHTED_PRESSURE_PLATE, // Gold
                Material.HEAVY_WEIGHTED_PRESSURE_PLATE, // Iron
                Material.CRIMSON_PRESSURE_PLATE,
                Material.POLISHED_BLACKSTONE_PRESSURE_PLATE,
                Material.WARPED_PRESSURE_PLATE,
                Material.STONE_PRESSURE_PLATE,
                Material.ACACIA_PRESSURE_PLATE,
                Material.BIRCH_PRESSURE_PLATE,
                Material.DARK_OAK_PRESSURE_PLATE,
                Material.JUNGLE_PRESSURE_PLATE,
                Material.OAK_PRESSURE_PLATE,
                Material.SPRUCE_PRESSURE_PLATE,
        };
    }
}
