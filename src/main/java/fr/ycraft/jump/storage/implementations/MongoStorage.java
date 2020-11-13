package fr.ycraft.jump.storage.implementations;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.JumpPlayer;
import lombok.RequiredArgsConstructor;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class MongoStorage implements StorageImplementation {
    private final Config config;
    private MongoDatabase database;
    private MongoClient client;

    @Override
    public void init() {
        this.client  =  MongoClients.create(String.format(
                "mongodb://%s:%s@%s:%s",
                this.config.get(Key.DATABASE_USER),
                this.config.get(Key.DATABASE_PASSWORD),
                this.config.get(Key.DATABASE_HOST),
                this.config.get(Key.DATABASE_PORT)
        ));

        this.database = this.client.getDatabase(config.get(Key.DATABASE_NAME));
    }

    @Override
    public void close() {
        this.client.close();
    }

    @Override
    public JumpPlayer loadPlayer(OfflinePlayer player) {
        // TODO load player
        return null;
    }

    @Override
    public List<JumpPlayer> loadAllPlayers() {
        // TODO load all players
        return null;
    }

    @Override
    public void storePlayer(JumpPlayer jumpPlayer) {
        // TODO store player
    }

    @Override
    public boolean deletePlayer(UUID id) {
        // TODO delete player
        return false;
    }

    @Override
    public List<Jump> loadJumps() {
        // TODO load jumps
        return null;
    }

    @Override
    public void storeJump(Jump jump) {
        // TODO store jump
    }

    @Override
    public boolean deleteJump(UUID id) {
        // TODO delete jump
        return false;
    }
}
