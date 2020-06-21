package fr.ycraft.jump.entity;

import fr.ycraft.jump.util.LocationUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class Jump implements ConfigurationSerializable {
    protected static final String NAME = "name";
    protected static final String SPAWN = "spawn";
    protected static final String START = "start";
    protected static final String END = "end";
    protected static final String CHECKPOINTS = "checkpoints";
    protected static final String BEST_SCORES = "best scores";
    protected static final String PLAYER = "player";
    protected static final String SCORE = "score";
    public static final List<Material> ALLOWED_MATERIALS = Collections.unmodifiableList(Arrays.asList(
            Material.GOLD_PLATE,
            Material.IRON_PLATE,
            Material.STONE_PLATE,
            Material.WOOD_PLATE
    ));

    private String name;
    private Location spawn, start, end;
    private final List<Location> checkpoints;
    private List<PlayerScore> bestScores;

    public Jump(String name) {
        this.name = name;
        this.spawn = null;
        this.start = null;
        this.end = null;
        this.checkpoints = new LinkedList<>();
        this.bestScores = new LinkedList<>();
    }

    public Jump(@NotNull String name, Location spawn, Location start, Location end, List<Location> checkpoints, @NotNull List<PlayerScore> bestScores) {
        this.name = name;
        this.spawn = spawn;
        this.start = start;
        this.end = end;
        this.checkpoints = checkpoints != null ? checkpoints : new LinkedList<>();
        this.bestScores = new LinkedList<>(bestScores);
    }

    @NotNull
    public String getName() {
        return name;
    }

    public Optional<Location> getSpawn() {
        return Optional.ofNullable(spawn);
    }

    public Optional<Location> getStart() {
        return Optional.ofNullable(start);
    }

    public Optional<Location> getEnd() {
        return Optional.ofNullable(this.end);
    }

    @NotNull
    public List<Location> getCheckpoints() {
        return new LinkedList<>(checkpoints);
    }

    @NotNull
    public List<PlayerScore> getBestScores() {
        return bestScores;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public void setSpawn(@NotNull Location spawn) {
        this.spawn = LocationUtil.roundLocation(spawn);
    }

    public void setStart(Location start) {
        this.start = Optional.ofNullable(start).map(LocationUtil::roundLocation).orElse(null);
    }

    public void setEnd(Location end) {
        this.end = Optional.ofNullable(end).map(LocationUtil::roundLocation).orElse(null);
    }

    public void removeCheckpoint(@NotNull Location loc) {
        new LinkedList<>(this.checkpoints).stream()
                .filter(l -> l.getBlockX() == loc.getBlockX()
                        && l.getBlockY() == loc.getBlockY()
                        && l.getBlockZ() == loc.getBlockZ())
                .forEach(this.checkpoints::remove);
    }

    public void addCheckpoint(Location loc) {
        this.checkpoints.add(LocationUtil.roundLocation(loc));
    }

    public synchronized void registerScore(OfflinePlayer player, long time, int maxScores) {
        this.bestScores.add(new PlayerScore(player, time));
        this.bestScores.sort((a, b) -> (int) (a.getMillis() - b.getMillis()));

        if (bestScores.size() > maxScores) {
            this.bestScores = this.bestScores.subList(0, maxScores);
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();

        data.put(NAME, this.name);
        data.put(SPAWN, this.spawn);
        data.put(START, this.start);
        data.put(END, this.end);
        data.put(CHECKPOINTS, this.checkpoints);
        data.put(BEST_SCORES, this.bestScores);

        return data;
    }


    /**
     * Required method for deserialization
     *
     * @param args map to deserialize
     * @return deserialized jump
     * @throws IllegalArgumentException if the world don't exists
     * @see ConfigurationSerializable
     */
    public static Jump deserialize(Map<String, Object> args) {
        return Optional.ofNullable(args.get(NAME))
                .map(Object::toString)
                .map(name -> new Jump(
                name,
                LocationUtil.extractLocation(args.get(SPAWN)),
                LocationUtil.extractLocation(args.get(START)),
                LocationUtil.extractLocation(args.get(END)),
                Optional.ofNullable(args.get(CHECKPOINTS))
                        .filter(List.class::isInstance)
                        .map(obj -> (List<?>) obj)
                        .orElseGet(LinkedList::new)
                        .stream()
                        .map(LocationUtil::extractLocation)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()),
                Optional.ofNullable(args.get(BEST_SCORES))
                        .filter(List.class::isInstance)
                        .map(obj -> (List<?>) obj)
                        .orElseGet(LinkedList::new)
                        .stream()
                        .filter(PlayerScore.class::isInstance)
                        .map(PlayerScore.class::cast)
                        .filter(s -> s.getMillis() != 0)
                        .collect(Collectors.toList())
        )).orElse(null);
    }
}
