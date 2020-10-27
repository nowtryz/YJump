package fr.ycraft.jump.storage.implementations;

import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.*;
import net.nowtryz.mcutils.injection.DataFolder;
import net.nowtryz.mcutils.injection.PluginLogger;
import fr.ycraft.jump.manager.JumpManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.dizitart.no2.*;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;

import static org.dizitart.no2.Document.createDocument;
import static org.dizitart.no2.FindOptions.sort;
import static org.dizitart.no2.IndexOptions.indexOptions;
import static org.dizitart.no2.UpdateOptions.updateOptions;
import static org.dizitart.no2.filters.Filters.and;
import static org.dizitart.no2.filters.Filters.eq;

public class NitriteStorage implements StorageImplementation {
    private static final String DATABASE_FILENAME = "data.nitrite.db";
    private static final String JUMPS_COLLECTION = "jumps";
    private static final String SCORES_COLLECTION = "scores";
    private static final String PLAYERS_COLLECTION = "players";
    private static final String PLAYER = "player";
    private static final String JUMP = "jump";
    private static final String DURATION = "duration";
    private static final String DATE = "date";
    private static final String NAME = "name";
    private static final String ID = "uuid";

    private final Provider<JumpManager> jumpManager;
    private final File pluginFolder;
    private final Config config;
    private final Logger logger;

    private Nitrite database = null;
    private NitriteCollection jumps = null;
    private NitriteCollection scores = null;
    private NitriteCollection players = null;

    @Inject
    NitriteStorage(@DataFolder File dataFolder, Provider<JumpManager> jumpManager, @PluginLogger Logger logger, Config config) {
        this.pluginFolder = dataFolder;
        this.jumpManager = jumpManager;
        this.logger = logger;
        this.config = config;
    }

    @Override
    public void init() {
        this.logger.warning("The warnings related to slf4j bellow are due to NitriteDB, ignore it");
        this.database = Nitrite.builder()
                .compressed()
                .filePath(new File(this.pluginFolder, DATABASE_FILENAME))
                .openOrCreate();

        this.jumps = this.database.getCollection(JUMPS_COLLECTION);
        this.scores = this.database.getCollection(SCORES_COLLECTION);
        this.players = this.database.getCollection(PLAYERS_COLLECTION);

        if (!this.jumps.hasIndex(Jump.ID)) this.jumps.createIndex(Jump.ID, indexOptions(IndexType.Unique));
        if (!this.jumps.hasIndex(Jump.NAME)) this.jumps.createIndex(Jump.NAME, indexOptions(IndexType.Unique));
        if (!this.scores.hasIndex(PLAYER)) this.scores.createIndex(PLAYER, indexOptions(IndexType.Fulltext));
        if (!this.scores.hasIndex(JUMP)) this.scores.createIndex(JUMP, indexOptions(IndexType.Fulltext));
        if (!this.players.hasIndex(ID)) this.players.createIndex(ID, indexOptions(IndexType.Unique));
        if (!this.players.hasIndex(NAME)) this.players.createIndex(NAME, indexOptions(IndexType.Fulltext));
    }

    @Override
    public void close() {
        this.database.compact();
        this.database.close();
    }

    @Override
    public JumpPlayer loadPlayer(OfflinePlayer player) {
        this.players.update(eq(ID, player.getUniqueId()), playerToDocument(player), updateOptions(true));
        return this.loadPlayer(player.getUniqueId(), player.getName());
    }

    public JumpPlayer loadPlayer(UUID id, String name) {
        JumpPlayer jumpPlayer = new JumpPlayer(id, name);

        for (Jump jump : this.jumpManager.get().getJumps().values()) {
            Cursor cursor = this.scores.find(
                    and(eq(JUMP, jump.getId()),eq(PLAYER, id)),
                    sort(DURATION, SortOrder.Ascending).thenLimit(0, this.config.get(Key.MAX_SCORES_PER_PLAYER))
            );

            for (Document document : cursor) jumpPlayer.put(jump, document.get(DURATION, Long.class));
        }

        return jumpPlayer;
    }

    @Override
    public List<JumpPlayer> loadAllPlayers() {
        List<JumpPlayer> players = new ArrayList<>();
        Cursor cursor = this.players.find();

        for (Document document : cursor) {
            UUID id = document.get(ID, UUID.class);
            String name = document.get(NAME, String.class);
            players.add(this.loadPlayer(id, name));
        }

        return players;
    }

    @Override
    public void storePlayer(JumpPlayer player) {
        this.players.update(eq(ID, player.getId()), playerToDocument(player), updateOptions(true));
        for (Map.Entry<Jump, List<TimeScore>> entry : player.entrySet()) {
            Jump jump = entry.getKey();
            for (TimeScore timeScore : entry.getValue()) {
                this.scores.update(
                        and(eq(DATE, timeScore.getDate()), eq(PLAYER, player.getId())),
                        scoreToDocument(jump, player.getId(), timeScore),
                        updateOptions(true)
                );
            }
        }
    }

    @Override
    public boolean deletePlayer(UUID id) {
        WriteResult result = this.scores.remove(eq("player", id));
        return result.getAffectedCount() > 0;
    }

    @Override
    public List<Jump> loadJumps() {
        Cursor cursor = this.jumps.find();
        List<Jump> jumps = new ArrayList<>();

        for (Document document : cursor) {
            UUID uuid = document.get(Jump.ID, UUID.class);
            jumps.add(docToJump(document, this.loadJumpScores(uuid)));
        }
        return jumps;
    }

    public List<PlayerScore> loadJumpScores(UUID id) {
        List<PlayerScore> scores = new ArrayList<>();
        Cursor cursor = this.scores.find(
                eq(JUMP, id),
                sort(DURATION, SortOrder.Ascending).thenLimit(0, this.config.get(Key.MAX_SCORES_PER_JUMP))
        );

        for (Document document : cursor) scores.add(documentToScore(document));

        return scores;
    }

    @Override
    public void storeJump(Jump jump) {
        this.jumps.update(eq(Jump.ID, jump.getId()), jumpToDocument(jump), updateOptions(true));
    }

    @Override
    public boolean deleteJump(UUID id) {
        RemoveOptions removeOptions = new RemoveOptions();
        removeOptions.setJustOne(true);

        WriteResult result = this.jumps.remove(eq(Jump.ID, id), removeOptions);
        return result.getAffectedCount() > 0;
    }

    private static Document jumpToDocument(Jump jump) {
        return createDocument(Jump.ID, jump.getId())
                .put(Jump.NAME, jump.getName())
                .put(Jump.DESCRIPTION, jump.getDescription().orElse(null))
                .put(Jump.WORLD, Optional.ofNullable(jump.getWorld()).map(World::getName).orElse(null))
                .put(Jump.SPAWN, jump.getSpawnPos().orElse(null))
                .put(Jump.START, jump.getStartPos().orElse(null))
                .put(Jump.END, jump.getEndPos().orElse(null))
                .put(Jump.CHECKPOINTS, jump.getCheckpointsPositions())
                .put(Jump.ITEM, jump.getItem().serialize());
    }

    @SuppressWarnings("unchecked")
    private static Jump docToJump(Document document, List<PlayerScore> playerScores) {
        return Jump.builder()
                .id(document.get(Jump.ID, UUID.class))
                .name(document.get(Jump.NAME, String.class))
                .description(document.get(Jump.DESCRIPTION, String.class))
                .world(Optional.ofNullable(document.get(Jump.WORLD, String.class)).map(Bukkit::getWorld).orElse(null))
                .spawn(document.get(Jump.SPAWN, Position.class))
                .start(document.get(Jump.START, Position.class))
                .end(document.get(Jump.END, Position.class))
                .checkpoints(new ArrayList<>((List<Position>) document.get(Jump.CHECKPOINTS, List.class)))
                .bestScores(playerScores)
                .item(ItemStack.deserialize((Map<String, Object>) document.get(Jump.ITEM, Map.class)))
                .build();
    }

    private static Document scoreToDocument(Jump jump, UUID playerId, TimeScore score) {
        return createDocument(DATE, score.getDate())
                .put(PLAYER, playerId)
                .put(JUMP, jump.getId())
                .put(DURATION, score.getDuration());
    }

    private static Document playerToDocument(JumpPlayer player) {
        return createDocument(ID, player.getId())
                .put(NAME, player.getName());
    }

    private static Document playerToDocument(OfflinePlayer player) {
        return createDocument(ID, player.getUniqueId())
                .put(NAME, player.getName());
    }

    private static PlayerScore documentToScore(Document document) {
        return new PlayerScore(
                Bukkit.getOfflinePlayer(document.get(PLAYER, UUID.class)),
                new TimeScore(document.get(DURATION, Long.class), document.get(DATE, Long.class))
        );
    }
}
