package fr.ycraft.jump.storage.implementations;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.JumpPlayer;
import lombok.RequiredArgsConstructor;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class MongoStorage implements StorageImplementation {
    private final Config config;
    private MongoDatabase database;

    @Override
    public void init() {
        ServerAddress address = new ServerAddress("hostOne", 27018);
        MongoClient client =  MongoClients.create(
                MongoClientSettings.builder()
                        .applyToClusterSettings(builder -> builder.hosts(Collections.singletonList(address)))
                        .build());
        this.database = client.getDatabase("database");
    }

    @Override
    public void close() {

    }

    @Override
    public JumpPlayer loadPlayer(OfflinePlayer player) {
        return null;
    }

    @Override
    public List<JumpPlayer> loadAllPlayers() {
        return null;
    }

    @Override
    public void storePlayer(JumpPlayer jumpPlayer) {

    }

    @Override
    public boolean deletePlayer(UUID id) {
        return false;
    }

    @Override
    public List<Jump> loadJumps() {
        return null;
    }

    @Override
    public void storeJump(Jump jump) {

    }

    @Override
    public boolean deleteJump(UUID id) {
        return false;
    }
}
