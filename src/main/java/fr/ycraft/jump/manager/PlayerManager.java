package fr.ycraft.jump.manager;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.JumpPlayer;
import net.nowtryz.mcutils.injection.PluginLogger;
import fr.ycraft.jump.storage.Storage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class PlayerManager extends AbstractManager {
    private final Storage storage;
    private final Map<OfflinePlayer, JumpPlayer> players = new HashMap<>();
    private final Logger logger;

    @Inject
    PlayerManager(JumpPlugin plugin, Storage storage, @PluginLogger Logger logger) {
        super(plugin);
        this.storage = storage;
        this.logger = logger;
    }

    public void init() {
        Bukkit.getOnlinePlayers().parallelStream().forEach(this::load);
    }

    public Optional<JumpPlayer> getPlayer(OfflinePlayer player) {
        return Optional.ofNullable(this.players.get(player));
    }

    public void load(OfflinePlayer player) {
        this.storage.loadPlayer(player)
                .thenAccept(jumpPlayer -> this.players.put(player, jumpPlayer))
                .whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        this.logger.log(Level.SEVERE, "Unable to load player " + player.getName(), throwable);
                    }
                });
    }

    public void unload(OfflinePlayer player) {
        this.getPlayer(player).ifPresent(jumpPlayer -> {
            this.storage.storePlayer(jumpPlayer);
            this.players.remove(player, jumpPlayer);
        });
    }

    public void save() {
        CompletableFuture.allOf(this.players.values()
                .parallelStream()
                .map(this.storage::storePlayer)
                .toArray(CompletableFuture[]::new)).join();
    }
}
