package fr.ycraft.jump.entity;

import com.google.common.collect.ImmutableList;
import lombok.*;
import lombok.EqualsAndHashCode.Include;
import net.nowtryz.mcutils.ItemStackUtil;
import org.bukkit.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static lombok.Builder.Default;

/**
 * Represents a parkour
 */
@Builder
@Getter @Setter
@SerializableAs("Jump")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Jump implements ConfigurationSerializable {
    public static final String
            ID = "id", NAME = "name", SPAWN = "spawn", START = "start", END = "end",
            CHECKPOINTS = "checkpoints", BEST_SCORES = "best scores", ITEM = "item",
            DESCRIPTION = "description", WORLD = "world";
    protected static Material DEFAULT_MATERIAL = Material.SLIME_BLOCK;
    public static final List<Material> ALLOWED_MATERIALS = Collections.unmodifiableList(Arrays.asList(
            Material.GOLD_PLATE,
            Material.IRON_PLATE,
            Material.STONE_PLATE,
            Material.WOOD_PLATE
    ));

    public static void setDefaultMaterial(@NonNull Material material) {
        DEFAULT_MATERIAL = material;
    }

    @Include @NonNull @Default
    private final UUID id = UUID.randomUUID();

    @Setter @NonNull
    private String name;

    @Getter @Setter
    private World world;

    @NonNull @Default
    private final List<Position> checkpoints = new ArrayList<>();

    @NonNull @Default
    private List<PlayerScore> bestScores = new ArrayList<>();

    @NonNull @Default
    private ItemStack item = new ItemStack(DEFAULT_MATERIAL);

    private String description;
    private Position spawn, start, end;


    public static class JumpBuilder {
        public JumpBuilder spawn(Position spawn) {
            this.spawn = spawn;
            return this;
        }

        public JumpBuilder start(Position start) {
            this.start = start;
            return this;
        }

        public JumpBuilder end(Position end) {
            this.end = end;
            return this;
        }

        public JumpBuilder spawn(Location spawn) {
            return this.spawn(Position.fromLocation(spawn));
        }

        public JumpBuilder start(Location start) {
            return this.spawn(Position.fromLocation(start));
        }

        public JumpBuilder end(Location end) {
            return this.spawn(Position.fromLocation(end));
        }

        public JumpBuilder item(@NonNull ItemStack item) {
            this.item$value = item.clone();
            this.item$set = true;
            ItemStackUtil.clearEnchants(this.item$value);
            return this;
        }
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Optional<Position> getSpawnPos() {
        return Optional.ofNullable(spawn);
    }

    public Optional<Position> getStartPos() {
        return Optional.ofNullable(start);
    }

    public Optional<Position> getEndPos() {
        return Optional.ofNullable(this.end);
    }

    public Optional<Location> getSpawn() {
        if (this.world == null) return Optional.empty();
        return this.getSpawnPos().map(position -> position.toLocation(this.getWorld()));
    }

    public Optional<Location> getStart() {
        if (this.world == null) return Optional.empty();
        return this.getStartPos().map(position -> position.toLocation(this.getWorld()));
    }

    public Optional<Location> getEnd() {
        if (this.world == null) return Optional.empty();
        return this.getEndPos().map(position -> position.toLocation(this.getWorld()));
    }

    public @NotNull List<Position> getCheckpointsPositions() {
        return ImmutableList.copyOf(checkpoints);
    }

    public @NotNull List<Location>  getCheckpoints() {
        if (this.world == null) return Collections.emptyList();
        return checkpoints
                .parallelStream()
                .map(position -> position.toLocation(this.world))
                .collect(ImmutableList.toImmutableList());
    }


    public void setSpawn(@NonNull Location spawn) {
        this.world = spawn.getWorld();
        this.spawn = Position.fromLocation(spawn);
    }

    public void setStart(@Nullable Location start) {
        if (start != null) this.world = start.getWorld();
        this.start = Position.fromLocation(start);
    }

    public void setEnd(@Nullable Location end) {
        this.end = Position.fromLocation(end);
    }

    /**
     * Remove a checkpoint from this jump
     * @param loc the location to remove from this jump
     */
    public void removeCheckpoint(@NotNull Location loc) {
        Position position = Position.fromLocation(loc);
        this.checkpoints.removeIf(position::equals);
    }

    public void addCheckpoint(@NonNull Location loc) {
        this.checkpoints.add(Position.fromLocation(loc));
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
        data.put(WORLD, Optional.ofNullable(this.world).map(World::getName).orElse(null));

        return data;
    }


    /**
     * Required method for deserialization
     *
     * @param args map to deserialize
     * @return deserialized jump
     * @see ConfigurationSerializable
     */
    @SuppressWarnings("unused")
    public static Jump deserialize(Map<String, Object> args) {
        try {
            return Optional.ofNullable(args.get(NAME))
                    .map(Object::toString)
                    .map(name -> builder()
                            .name(name)
                            .id(Optional.ofNullable(args.get(ID))
                                    .map(Object::toString)
                                    .map(UUID::fromString)
                                    .orElseGet(UUID::randomUUID))
                            .description(Optional.ofNullable(args.get(DESCRIPTION))
                                    .map(Object::toString)
                                    .orElse(null))
                            .spawn(Position.extract(args.get(SPAWN)))
                            .start(Position.extract(args.get(START)))
                            .end(Position.extract(args.get(END)))
                            .world(Optional.ofNullable(args.get(WORLD))
                                    .map(Object::toString)
                                    .map(Bukkit::getWorld)
                                    .orElse(null))
                            .bestScores(Optional.ofNullable(args.get(BEST_SCORES))
                                    .filter(List.class::isInstance)
                                    .map(obj -> (List<?>) obj)
                                    .orElseGet(LinkedList::new)
                                    .stream()
                                    .filter(PlayerScore.class::isInstance)
                                    .map(PlayerScore.class::cast)
                                    .filter(s -> s.getMillis() != 0)
                                    .collect(Collectors.toList()))
                            .item(Optional.ofNullable(args.get(ITEM))
                                    .filter(ItemStack.class::isInstance)
                                    .map(ItemStack.class::cast)
                                    .orElseGet(() -> new ItemStack(Jump.DEFAULT_MATERIAL)))
                            .checkpoints(Optional.ofNullable(args.get(CHECKPOINTS))
                                    .filter(List.class::isInstance)
                                    .map(obj -> (List<?>) obj)
                                    .orElseGet(LinkedList::new)
                                    .stream()
                                    .map(Position::extract)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList()))
                            .build()
                    ).orElse(null);
        } catch (Exception exception) {
            System.err.println("[YJump] Unable to deserialize jump");
            exception.printStackTrace();
            return null;
        }

    }
}
