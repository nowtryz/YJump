package fr.ycraft.jump.util.material;

import net.nowtryz.mcutils.MCUtils;
import org.bukkit.Material;

public interface MaterialResolver {
    Material[] getPlates();
    Material getDefaultPlate();
    Material getEndIcon();

    static MaterialResolver getResolver() {
        if (MCUtils.SIXTEEN_COMPATIBLE) return new SixteenResolver();
        else if (MCUtils.THIRTEEN_COMPATIBLE) return new ThirteenToFifteenResolver();
        else return new TwelveResolver();
    }
}
