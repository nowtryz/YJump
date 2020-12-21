package fr.ycraft.jump.util.material;

import org.bukkit.Material;

public class TwelveResolver implements MaterialResolver {
    private final static Material GOLD = Material.valueOf("GOLD_PLATE");
    private final static Material IRON = Material.valueOf("IRON_PLATE");
    private final static Material STONE = Material.valueOf("STONE_PLATE");
    private final static Material WOOD = Material.valueOf("WOOD_PLATE");
    private final static Material END_STONE = Material.valueOf("ENDER_STONE");

    @Override
    public Material[] getPlates() {
        return new Material[]{GOLD, IRON, STONE, WOOD};
    }

    @Override
    public Material getDefaultPlate() {
        return GOLD;
    }

    @Override
    public Material getGoldPlate() {
        return GOLD;
    }

    @Override
    public Material getIronPlate() {
        return IRON;
    }

    @Override
    public Material getEndIcon() {
        return END_STONE;
    }
}
