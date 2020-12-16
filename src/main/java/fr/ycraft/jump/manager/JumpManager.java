package fr.ycraft.jump.manager;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.mu.util.stream.BiStream;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.storage.Storage;
import lombok.Getter;
import net.nowtryz.mcutils.LocationUtil;
import net.nowtryz.mcutils.injection.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Getter
@Singleton
public class JumpManager extends AbstractManager {
    private final Storage storage;
    private final Logger logger;
    private final Config config;

    protected Map<String, Jump> jumps;
    protected Map<UUID, Jump> jumpsById;
    protected Map<Location, Jump> jumpStarts;
    protected List<Location> protectedLocations;
    protected List<World> protectedWorlds;

    @Inject
    JumpManager(JumpPlugin plugin, Storage storage, @PluginLogger Logger logger, Config config) {
        super(plugin);
        this.storage = storage;
        this.logger = logger;
        this.config = config;
    }

    public void init() {
        try {
            this.storage.loadJumps().thenAccept(this::updateJumpList).get();
            this.showJumpList();
        } catch (InterruptedException | ExecutionException e) {
            this.logger.log(Level.SEVERE, "Unable to load jumps", e);
            Bukkit.getPluginManager().disablePlugin(this.plugin);
        }
    }

    public void updateJumpList() {
        this.updateJumpList(new ArrayList<>(this.jumps.values()));
    }

    public synchronized void updateJumpList(List<Jump> jumps) {
        this.jumps = BiStream.from(jumps, Jump::getName, Function.identity()).toMap();
        this.jumpsById = BiStream.from(jumps, Jump::getId, Function.identity()).toMap();

        // only keep playable jumps
        this.jumpStarts = jumps.parallelStream()
                .filter(jump -> jump.getStart().isPresent())
                .filter(jump -> jump.getEnd().isPresent())
                .collect(Collectors.toMap(
                        jump -> LocationUtil.toBlock(jump.getStart().get()),
                        Function.identity())
                );

        List<Location> protectedLocations = new ArrayList<>();
        protectedLocations.addAll(this.jumpStarts.keySet());
        protectedLocations.addAll(this.jumps.values().stream().parallel()
                .map(Jump::getEnd)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
        protectedLocations.addAll(jumps.parallelStream()
                .map(Jump::getCheckpoints)
                .flatMap(List::parallelStream)
                .collect(Collectors.toList())
        );

        this.protectedLocations = protectedLocations.parallelStream()
                .map(LocationUtil::toBlock)
                .collect(ImmutableList.toImmutableList());
        this.protectedWorlds = jumps.parallelStream()
                .map(Jump::getWorld)
                .filter(Objects::nonNull)
                .distinct()
                .collect(ImmutableList.toImmutableList());
    }

    private void showJumpList() {
        String[] jumps = this.jumps.keySet().toArray(new String[0]);
        if (jumps.length == 0) this.logger.warning("No jump loaded");
        else this.logger.info(String.format(
                "Loaded the following jumps: %s",
                String.join(", ", jumps)
        ));

        this.jumps.values()
                .parallelStream()
                .filter(jump -> jump.getWorld() == null)
                .forEach(jump -> this.getLogger().warning(String.format(
                        "The world of `%1$s` is not set or have changed, " +
                                "please update it with `/jump setworld %1$s <world>`",
                        jump.getName()
                )));
    }

    public void replacePlates() {
        this.jumps.values().forEach(jump -> {
            // Place blocks bellow plates
            jump.getStart()
                    .map(Location::getBlock)
                    .map(block -> block.getRelative(BlockFace.DOWN))
                    .filter(b -> !b.getType().isOccluding())
                    .ifPresent(b -> b.setType(Material.GOLD_BLOCK));
            jump.getEnd()
                    .map(Location::getBlock)
                    .map(block -> block.getRelative(BlockFace.DOWN))
                    .filter(b -> !b.getType().isOccluding())
                    .ifPresent(b -> b.setType(Material.GOLD_BLOCK));
            jump.getCheckpoints()
                    .stream()
                    .map(Location::getBlock)
                    .map(block -> block.getRelative(BlockFace.DOWN))
                    .filter(b -> !b.getType().isOccluding())
                    .forEach(b -> b.setType(Material.GOLD_BLOCK));
            // Place plates
            jump.getStart()
                    .map(Location::getBlock)
                    .filter(b -> !b.getType().equals(this.config.get(Key.START_MATERIAL)))
                    .ifPresent(b -> b.setType(this.config.get(Key.START_MATERIAL)));
            jump.getEnd()
                    .map(Location::getBlock)
                    .filter(b -> !b.getType().equals(this.config.get(Key.END_MATERIAL)))
                    .ifPresent(b -> b.setType(this.config.get(Key.END_MATERIAL)));
            jump.getCheckpoints()
                    .stream()
                    .map(Location::getBlock)
                    .filter(b -> !b.getType().equals(this.config.get(Key.CHECKPOINT_MATERIAL)))
                    .forEach(b -> b.setType(this.config.get(Key.CHECKPOINT_MATERIAL)));
        });
    }

    public Optional<Jump> getJump(String name) {
        return Optional.ofNullable(this.jumps.get(name));
    }

    public Jump createAndSave(String name) {
        Jump jump =Jump.builder()
                .name(name)
                .fallDistance(this.config.get(Key.MAX_FALL_DISTANCE))
                .build();
        this.storage.storeJump(jump);
        this.jumps.put(name, jump);
        return jump;
    }

    public void delete(Jump jump) {
        this.storage.deleteJump(jump).whenComplete((result, throwable) -> {
            if (result) this.jumps.remove(jump.getName(), jump);
            else this.plugin.getLogger().log(Level.SEVERE, "Unable to delete jump", throwable);
        });
    }

    public void save() {
        if (this.jumps == null) return;
        CompletableFuture<?>[] completableFutures = this.jumps.values()
                .parallelStream()
                .map(this.storage::storeJump)
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(completableFutures).join();
    }
}
