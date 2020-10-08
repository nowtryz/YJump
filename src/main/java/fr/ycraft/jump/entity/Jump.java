package fr.ycraft.jump.entity;

import fr.ycraft.jump.util.ItemStackUtil;
import fr.ycraft.jump.util.LocationUtil;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Getter @Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Jump implements ConfigurationSerializable {
    protected static final String
            ID = "id", NAME = "name", SPAWN = "spawn", START = "start", END = "end",
            CHECKPOINTS = "checkpoints", BEST_SCORES = "best scores", ITEM = "item",
            DESCRIPTION = "description";
    protected static final Material DEFAULT_MATERIAL = Material.SLIME_BLOCK;
    public static final List<Material> ALLOWED_MATERIALS = Collections.unmodifiableList(Arrays.asList(
            Material.GOLD_PLATE,
            Material.IRON_PLATE,
            Material.STONE_PLATE,
            Material.WOOD_PLATE
    ));

    @Include @NonNull
    private final UUID id;
    @Setter @NonNull
    private String name;
    private String description;
    private Location spawn, start, end;
    private final List<Location> checkpoints;
    private @NotNull List<PlayerScore> bestScores;
    private ItemStack item;

    public Jump(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = null;
        this.spawn = null;
        this.start = null;
        this.end = null;
        this.checkpoints = new LinkedList<>();
        this.bestScores = new LinkedList<>();
        this.item = new ItemStack(DEFAULT_MATERIAL);
    }

    public Jump(
            @NonNull UUID id,
            @NonNull String name,
            @Nullable String description,
            @Nullable Location spawn,
            @Nullable Location start,
            @Nullable Location end,
            @Nullable List<Location> checkpoints,
            @NonNull List<PlayerScore> bestScores,
            @NonNull ItemStack item) {
        this.id = id;
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

    public @NotNull List<Location> getCheckpoints() {
        return new LinkedList<>(checkpoints);
    }


    public void setSpawn(@NotNull Location spawn) {
        this.spawn = LocationUtil.toCheckpoint(spawn);
    }

    public void setStart(Location start) {
        this.start = Optional.ofNullable(start).map(LocationUtil::toCheckpoint).orElse(null);
    }

    public void setEnd(Location end) {
        this.end = Optional.ofNullable(end).map(LocationUtil::toCheckpoint).orElse(null);
    }

    /**
     * Remove a checkpoint from this jump
     * @param loc the location to remove from this jump
     */
    public void removeCheckpoint(@NotNull Location loc) {
        new LinkedList<>(this.checkpoints).stream()
                .filter(l -> LocationUtil.isBlockLocationEqual(l, loc))
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
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();

        data.put(ID, this.id.toString());
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
        try {
            return Optional.ofNullable(args.get(NAME))
                    .map(Object::toString)
                    .map(name -> new Jump(
                            Optional.ofNullable(args.get(ID))
                                    .map(Object::toString)
                                    .map(UUID::fromString)
                                    .orElseGet(UUID::randomUUID),
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
                                    .orElseGet(() -> new ItemStack(Jump.DEFAULT_MATERIAL))
                    )).orElse(null);
        } catch (Exception exception) {
            System.err.println("[YJump] Unable to deserialize jump");
            exception.printStackTrace();
            return null;
        }

    }
}
