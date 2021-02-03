package fr.ycraft.jump.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a 3d-dimensional position, world independent, that can be converted to a Bukkit's
 * {@link org.bukkit.Location}
 */
@Data
@SerializableAs("Pos")
@AllArgsConstructor
@RequiredArgsConstructor
public class Position implements Cloneable, ConfigurationSerializable, Serializable {
    private static final long serialVersionUID = -3140111461271073006L;
    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";
    private static final String PITCH = "pitch";
    private static final String YAW = "yaw";

    /**
     * The x-coordinate of this position
     * @param x X-coordinate to set
     * @return x-coordinate
     */
    private int x;

    /**
     * The y-coordinate of this position
     * @param y Y-coordinate to set
     * @return y-coordinate
     */
    private int y;

    /**
     * The z-coordinate of this position
     * @param z Z-coordinate to set
     * @return z-coordinate
     */
    private int z;

    /**
     * The pitch of this position, measured in degrees.
     * <ul>
     * <li>A pitch of 0 represents level forward facing.
     * <li>A pitch of 90 represents downward facing, or negative y
     *     direction.
     * <li>A pitch of -90 represents upward facing, or positive y direction.
     * </ul>
     * Increasing pitch values the equivalent of looking down.
     *
     * @param pitch new incline's pitch
     * @return the incline's pitch
     */
    private float pitch = 0f;

    /**
     * The yaw of this position, measured in degrees.
     * <ul>
     * <li>A yaw of 0 or 360 represents the positive z direction.
     * <li>A yaw of 180 represents the negative z direction.
     * <li>A yaw of 90 represents the negative x direction.
     * <li>A yaw of 270 represents the positive x direction.
     * </ul>
     * Increasing yaw values are the equivalent of turning to your
     * right-facing, increasing the scale of the next respective axis, and
     * decreasing the scale of the previous axis.
     *
     * @param yaw new rotation's yaw
     * @return the rotation's yaw
     */
    private float yaw = 0f;

    /**
     * Create a location from this position
     * @param world in witch create the location
     * @return the location corresponding to this position in the given world
     */
    public Location toLocation(@NonNull World world) {
        return new Location(world, this.x + .5, this.y, this.z + .5, this.yaw, this.pitch);
    }

    /**
     * Checks if this position and the given location are the same block
     * @param location the location to check
     * @return true if it is the case
     */
    public boolean isBlock(@Nullable Location location) {
        return location != null
                && this.x == location.getBlockX()
                && this.y == location.getBlockY()
                && this.z == location.getBlockZ();
    }

    /**
     * Get a new position.
     *
     * @return a clone of this position
     */
    @Override
    public Position clone() {
        try {
            return (Position) super.clone();
        } catch (CloneNotSupportedException exception) {
            // clone is supported, but just in case we log it
            exception.printStackTrace();
            return this;
        }
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();

        result.put(X, getX());
        result.put(Y, getY());
        result.put(Z, getZ());

        result.put(YAW, this.yaw);
        result.put(PITCH, this.pitch);

        return result;
    }

    /**
     * Required method for deserialization
     *
     * @param args map to deserialize
     * @return deserialized position
     * @see ConfigurationSerializable
     */
    @SuppressWarnings("unused")
    public static Position deserialize(Map<String, Object> args) {
        return new Position(
                args.containsKey(X) ? NumberConversions.toInt(args.get(X)) : 0,
                args.containsKey(Y) ? NumberConversions.toInt(args.get(Y)) : 0,
                args.containsKey(Z) ? NumberConversions.toInt(args.get(Z)) : 0,
                args.containsKey(PITCH) ? NumberConversions.toInt(args.get(PITCH)) : 0,
                args.containsKey(YAW) ? NumberConversions.toInt(args.get(YAW)) : 0
        );
    }

    /**
     * Extract a position from the give location
     * @param location the location to get coordinates from
     * @return a position corresponding to the given location
     */
    @Contract("null -> null; !null -> !null")
    public static Position fromLocation(@Nullable Location location) {
        if (location == null) return null;
        return new Position(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getPitch(),
                location.getYaw()
        );
    }

    public static Position extract(Object obj) {
        if (obj instanceof Location) return fromLocation((Location) obj);
        if (obj instanceof Position) return (Position) obj;
        return null;
    }
}
