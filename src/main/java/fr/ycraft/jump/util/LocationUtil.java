package fr.ycraft.jump.util;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class LocationUtil {
    public static @NotNull Location roundLocation(@NotNull Location base) {
        Location loc = base.clone();
        loc.setX(loc.getBlockX());
        loc.setY(loc.getBlockY());
        loc.setZ(loc.getBlockZ());
        return loc;
    }

    public static @NotNull Location toBlock(Location base) {
        Location loc = roundLocation(base);
        loc.setPitch(0);
        loc.setYaw(0);
        return loc;
    }

    public static @NotNull Location toCheckpoint(@NotNull Location base) {
        Location loc = base.clone();
        loc.setX(loc.getBlockX() + .5);
        loc.setY(loc.getBlockY());
        loc.setZ(loc.getBlockZ() + .5);
        return loc;
    }

    public static boolean isBlockLocationEqual(@NotNull Location a, @NotNull Location b) {
        return (a.getBlockX() == b.getBlockX()
                && a.getBlockY() == b.getBlockY()
                && a.getBlockZ() == b.getBlockZ()
        );
    }

    public static Location extractLocation(Object o) {
        if (o instanceof Location) return LocationUtil.toCheckpoint((Location) o);
        return null;
    }
}
