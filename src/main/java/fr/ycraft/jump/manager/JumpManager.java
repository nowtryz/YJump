package fr.ycraft.jump.manager;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.mu.util.stream.BiStream;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.injection.PluginLogger;
import fr.ycraft.jump.storage.Storage;
import lombok.Getter;
import net.nowtryz.mcutils.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

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

    protected Map<String, Jump> jumps;
    protected Map<UUID, Jump> jumpsById;
    protected Map<Location, Jump> jumpStarts;
    protected List<Location> protectedLocations;
    protected List<World> protectedWorlds;

    @Inject
    JumpManager(JumpPlugin plugin, Storage storage, @PluginLogger Logger logger) {
        super(plugin);
        this.storage = storage;
        this.logger = logger;
    }

    public void init() {
        try {
            this.storage.loadJumps().thenAccept(this::updateJumpList).get();
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

    public Optional<Jump> getJump(String name) {
        return Optional.ofNullable(this.jumps.get(name));
    }

    public Jump createAndSave(String name) {
        Jump jump =Jump.builder()
                .name(name)
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
        CompletableFuture<?>[] completableFutures = this.jumps.values()
                .parallelStream()
                .map(this.storage::storeJump)
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(completableFutures).join();
    }
}
