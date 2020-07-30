package fr.ycraft.jump.entity;

import fr.ycraft.jump.util.ItemStackUtil;
import fr.ycraft.jump.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class Jump implements ConfigurationSerializable {
    protected static final String NAME = "name", SPAWN = "spawn", START = "start", END = "end";
    protected static final String CHECKPOINTS = "checkpoints", BEST_SCORES = "best scores", ITEM = "item";
    protected static final String DESCRIPTION = "description";
    protected static final Material defaultMaterial = Material.SLIME_BLOCK;
    public static final List<Material> ALLOWED_MATERIALS = Collections.unmodifiableList(Arrays.asList(
            Material.GOLD_PLATE,
            Material.IRON_PLATE,
            Material.STONE_PLATE,
            Material.WOOD_PLATE
    ));

    private String name, description;
    private Location spawn, start, end;
    private final List<Location> checkpoints;
    private List<PlayerScore> bestScores;
    private ItemStack item;

    public Jump(String name) {
        this.name = name;
        this.description = null;
        this.spawn = null;
        this.start = null;
        this.end = null;
        this.checkpoints = new LinkedList<>();
        this.bestScores = new LinkedList<>();
        this.item = new ItemStack(Jump.defaultMaterial);
    }

    public Jump(
            @NotNull String name,
            @Nullable String description,
            @Nullable Location spawn,
            @Nullable Location start,
            @Nullable Location end,
            @Nullable List<Location> checkpoints,
            @NotNull List<PlayerScore> bestScores,
            @NotNull ItemStack item) {
        this.name = name;
        this.description = description;
        this.spawn = spawn;
        this.start = start;
        this.end = end;
        this.checkpoints = checkpoints != null ? checkpoints : new LinkedList<>();
        this.bestScores = new LinkedList<>(bestScores);
        this.item = item;

        ItemStackUtil.clearEnchants(item);
    }

    @NotNull
    public String getName() {
        return name;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
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

    @NotNull
    public ItemStack getItem() {
        return item;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
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

    /**
     * Remove a from this jump (only in cache), this update must be performed by a JumpManager to ensure
     * that caches are coherent with the storage as, depending on the storage use, the update may nat be
     * taken into account. If you want to ensure that the checkpoint is removed from the storage, use
     * {@link fr.ycraft.jump.manager.JumpManager#deleteCheckpoint(Jump, Location) JumpManager#deleteCheckpoint()}
     * instead
     * @param loc the location to remove from this jump
     */
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

    public void setItem(ItemStack item) {
        this.item = item.clone();
        ItemStackUtil.clearEnchants(this.item);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Jump)) return false;
        Jump jump = (Jump) o;
        return name.equals(jump.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();

        data.put(NAME, this.name);
        data.put(DESCRIPTION, this.description);
        data.put(SPAWN, this.spawn);
        data.put(START, this.start);
        data.put(END, this.end);
        data.put(CHECKPOINTS, this.checkpoints);
        data.put(BEST_SCORES, this.bestScores);
        data.put(ITEM, this.item);

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
                // Description
                Optional.ofNullable(args.get(DESCRIPTION))
                        .map(Object::toString)
                        .orElse(null),
                // Spawn position
                LocationUtil.extractLocation(args.get(SPAWN)),
                // Start position
                LocationUtil.extractLocation(args.get(START)),
                // End position
                LocationUtil.extractLocation(args.get(END)),
                // Checkpoints list
                Optional.ofNullable(args.get(CHECKPOINTS))
                        .filter(List.class::isInstance)
                        .map(obj -> (List<?>) obj)
                        .orElseGet(LinkedList::new)
                        .stream()
                        .map(LocationUtil::extractLocation)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()),
                // Scores list
                Optional.ofNullable(args.get(BEST_SCORES))
                        .filter(List.class::isInstance)
                        .map(obj -> (List<?>) obj)
                        .orElseGet(LinkedList::new)
                        .stream()
                        .filter(PlayerScore.class::isInstance)
                        .map(PlayerScore.class::cast)
                        .filter(s -> s.getMillis() != 0)
                        .collect(Collectors.toList()),
                // Logo Item
                Optional.ofNullable(args.get(ITEM))
                        .filter(ItemStack.class::isInstance)
                        .map(ItemStack.class::cast)
                        .orElseGet(() -> new ItemStack(Jump.defaultMaterial))
        )).orElse(null);
    }
}
