package fr.ycraft.jump.manager;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class JumpManager extends AbstractManager {
    protected Map<String, Jump> jumps;
    protected Map<Location, Jump> jumpStarts;
    protected List<Location> protectedLocations;
    protected List<World> protectedWorlds;

    public JumpManager(JumpPlugin plugin) {
        super(plugin);
    }

    public void updateJumpList() {
        this.jumpStarts = this.jumps
                .values()
                .stream().parallel()
                .filter(jump -> jump.getStart().isPresent())
                .filter(jump -> jump.getEnd().isPresent())
                .collect(Collectors.toMap(jump -> jump.getStart().get(), Function.identity()));

        List<Location> protectedLocations = new ArrayList<>();
        protectedLocations.addAll(this.jumpStarts.keySet());
        protectedLocations.addAll(this.jumps.values().stream().parallel()
                .map(Jump::getEnd)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
        protectedLocations.addAll(this.jumps.values().stream().parallel()
                .map(Jump::getCheckpoints)
                .flatMap(List::stream)
                .collect(Collectors.toList())
        );

        this.protectedLocations = protectedLocations.stream().parallel()
                .map(LocationUtil::toBlock)
                .collect(Collectors.toList());
        this.protectedWorlds = this.protectedLocations.stream().parallel()
                .map(Location::getWorld)
                .distinct()
                .collect(Collectors.toList());
    }

    // Getters

    public Map<String, Jump> getJumps() {
        return this.jumps;
    }

    public Map<Location, Jump> getJumpStarts() {
        return jumpStarts;
    }

    public List<Location> getProtectedLocations() {
        return protectedLocations;
    }

    public List<World> getProtectedWorlds() {
        return protectedWorlds;
    }

    public Optional<Jump> getJump(String name) {
        return Optional.ofNullable(this.jumps.get(name));
    }

    // Utilities

    public abstract void persist(Jump jump);
    public abstract void updateName(Jump jump, String name);
    public abstract void deleteCheckpoint(Jump jump, Location location);
    public abstract void delete(Jump jump);
    public abstract void save();
}
