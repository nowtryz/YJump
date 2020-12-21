package fr.ycraft.jump.util.material;

import net.nowtryz.mcutils.MCUtils;
import org.bukkit.Material;

public interface MaterialResolver {
    MaterialResolver RESOLVER =
            MCUtils.SIXTEEN_COMPATIBLE ? new SixteenResolver() :
            MCUtils.THIRTEEN_COMPATIBLE ? new ThirteenToFifteenResolver() :
            new TwelveResolver();

    Material[] getPlates();
    Material getDefaultPlate();
    Material getGoldPlate();
    Material getIronPlate();
    Material getEndIcon();

    static Material[] plates() { return RESOLVER.getPlates(); }
    static Material defaultPlate() { return RESOLVER.getDefaultPlate(); }
    static Material goldPlate() { return RESOLVER.getGoldPlate(); }
    static Material ironPlate() { return RESOLVER.getIronPlate(); }
    static Material endStone() { return RESOLVER.getEndIcon(); }
    static MaterialResolver getResolver() { return RESOLVER; }
}
