package fr.ycraft.jump.storage;

import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.JumpPlayer;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;

public interface StorageImplementation {
    void init() throws Exception;
    void close() throws Exception;

    JumpPlayer loadPlayer(OfflinePlayer player) throws Exception;
    List<JumpPlayer> loadAllPlayers() throws Exception;
    void storePlayer(JumpPlayer jumpPlayer) throws Exception;
    boolean deletePlayer(UUID id) throws Exception;

    List<Jump> loadJumps() throws Exception;
    void storeJump(Jump jump) throws Exception;
    boolean deleteJump(UUID id) throws Exception;

    // for resets
//    Optional<UUID> getPlayerId(String name);
//    List<String> getKnownPlayers();

    interface Runnable {
        void run() throws Exception;
    }
}
