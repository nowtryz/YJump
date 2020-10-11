package fr.ycraft.jump.manager;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.JumpPlayer;
import fr.ycraft.jump.storage.Storage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class PlayerManager extends AbstractManager {
    private final Storage storage;
    private final Map<OfflinePlayer, JumpPlayer> players = new HashMap<>();

    @Inject
    PlayerManager(JumpPlugin plugin, Storage storage) {
        super(plugin);
        this.storage = storage;
    }

    public void init() {
        Bukkit.getOnlinePlayers().parallelStream().forEach(this::load);
    }

    public Optional<JumpPlayer> getPlayer(OfflinePlayer player) {
        return Optional.ofNullable(this.players.get(player));
    }

    public void load(OfflinePlayer player) {
        this.storage.loadPlayer(player).thenAccept(jumpPlayer -> this.players.put(player, jumpPlayer));
    }

    public void unload(OfflinePlayer player) {
        this.getPlayer(player).ifPresent(jumpPlayer -> {
            this.storage.storePlayer(jumpPlayer);
            this.players.remove(player, jumpPlayer);
        });
    }
}
