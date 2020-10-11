package fr.ycraft.jump.storage;

import com.google.inject.Inject;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.JumpPlayer;
import fr.ycraft.jump.injection.BukkitExecutor;
import fr.ycraft.jump.storage.implementations.StorageImplementation;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.OfflinePlayer;

import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Singleton
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Storage {
    Executor executor;
    StorageImplementation implementation;

    @Inject
    Storage(StorageImplementation implementation, @BukkitExecutor Executor executor) {
        this.implementation = implementation;
        this.executor = executor;
    }

    public void init() {
        try {
            this.implementation.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            this.implementation.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> CompletableFuture<T> future(Callable<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        }, this.executor);
    }

    private CompletableFuture<Void> future(StorageImplementation.Runnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        }, this.executor);
    }

    public CompletableFuture<JumpPlayer> loadPlayer(OfflinePlayer player) {
        return future(() -> this.implementation.loadPlayer(player));
    }

    public CompletableFuture<List<JumpPlayer>> loadAllPlayers() {
        return future(this.implementation::loadAllPlayers);
    }

    public CompletableFuture<Void> storePlayer(JumpPlayer jumpPlayer) {
        return future(() -> this.implementation.storePlayer(jumpPlayer));
    }

    public CompletableFuture<Boolean> deletePlayer(UUID id) {
        return future(() -> this.implementation.deletePlayer(id));
    }

    public CompletableFuture<List<Jump>> loadJumps() {
        return future(this.implementation::loadJumps);
    }

    public CompletableFuture<Void> storeJump(Jump jump) {
        return future(() -> this.implementation.storeJump(jump));
    }

    public CompletableFuture<Boolean> deleteJump(Jump jump) {
        return this.deleteJump(jump.getId());
    }

    public CompletableFuture<Boolean> deleteJump(UUID uuid) {
        return future(() -> this.implementation.deleteJump(uuid));
    }

//    public Optional<UUID> getPlayerId(String name) {
//        return this.implementation.getPlayerId(name);
//    }
//
//    public List<String> getKnownPlayers() {
//        return this.implementation.getKnownPlayers();
//    }
}
