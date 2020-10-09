package fr.ycraft.jump.storage.implementations;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.mu.util.stream.BiStream;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.*;
import fr.ycraft.jump.injection.PluginLogger;
import fr.ycraft.jump.util.DatabaseUtil;
import fr.ycraft.jump.util.UUIDUtils;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.intellij.lang.annotations.Language;

import javax.inject.Inject;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

@RequiredArgsConstructor(onConstructor_={@Inject})
@ExtensionMethod({MySQLStorage.class})
public class MySQLStorage implements StorageImplementation {
    private static final Gson gson = new Gson();
    @Language("SQL")
    private final static String
            SELECT_ALL_SCORES =       "SELECT s.`duration` 'duration', j.`name` 'jump_name'"+
                                      "FROM `jump_score` s " +
                                      "INNER JOIN `jump_jump` j " +
                                      "ON s.`jump_id` = j.`id` " +
                                      "WHERE s.`player` = UNHEX(REPLACE(?, '-','')) " +
                                      "ORDER BY `duration` DESC",
            SELECT_SCORES =           SELECT_ALL_SCORES + " LIMIT ?",
            UPSERT_SCORES =           "INSERT INTO `jump_score`(player, duration, jump_id) " +
                                      "VALUE (" +
                                          "UNHEX(REPLACE(?, '-','')),?," +
                                          "(SELECT `jump_id` FROM `jump_jump` WHERE `name` = ?)" +
                                      ") ON DUPLICATE KEY UPDATE duration=duration",
            DELETE_PLAYER =           "DELETE FROM `jump_score` WHERE `player` = UNHEX(REPLACE(?, '-',''))",
            DELETE_JUMP =             "DELETE FROM `jump_jump` WHERE `id` = ?",
            SELECT_JUMPS =            "SELECT id, name, description, spawn, start, end FROM `jump_jump`",
            UPDATE_JUMP =             "UPDATE `jump_jump` SET `name` = ?, `description` = ?, `item` = ? WHERE `id` = ?",
            JUMP_EXISTS =             "SELECT 1 FROM `jump_jump` WHERE `id` = ?",
            INSERT_JUMP =             "INSERT INTO `jump_jump`(`name`, `description`,`item`, `id`) VALUE (?,?,?,?)",
            UPDATE_JUMP_LOCATION =    "UPDATE `jump_jump` SET %s = ? WHERE id = ?",
            INSERT_CHECKPOINT =       "INSERT IGNORE INTO `jump_checkpoints` (`jump`, `location`) VALUE (?,?)",
            DELETE_CHECKPOINT =       "DELETE FROM `jump_checkpoints` WHERE `jump` = ? AND `location` = ?",
            UPSERT_LOCATION =         "INSERT INTO `jump_location` (world,x,y,z,pitch,yaw,hash) " +
                                      "VALUE (?,?,?,?,?,?,?) " +
                                      "ON DUPLICATE KEY UPDATE `hash` = hash",
            DELETE_UNUSED_LOCATIONS = "DELETE FROM `jump_location` " +
                                      "WHERE `hash` NOT IN (SELECT `location` FROM `jump_checkpoints`) " +
                                      "AND `hash` NOT IN (SELECT `spawn` FROM `jump_jump`) " +
                                      "AND `hash` NOT IN (SELECT `start` FROM `jump_jump`) " +
                                      "AND `hash` NOT IN (SELECT `end` FROM `jump_jump`) ";

    private final Map<Jump, Long> ids = new ConcurrentHashMap<>();
    private final JumpPlugin plugin;
    private final Config config;
    private Connection connection;

    @Inject
    @PluginLogger
    private Logger logger;

    @Override
    public void init() throws SQLException {
        DatabaseUtil.initDatabase(this.plugin);
        this.connection = DatabaseUtil.createConnection(plugin.getConfigProvider());

        logger.info(String.format(
                "Using %s database on %s:%d",
                config.get(Key.DATABASE_NAME),
                config.get(Key.DATABASE_HOST),
                config.get(Key.DATABASE_PORT)
        ));
    }

    @Override
    public void close() throws SQLException {
        this.cleanupLocations();
        this.connection.close();
    }

    @Override
    public JumpPlayer loadPlayer(OfflinePlayer player) throws SQLException {
        return this.loadPlayer(player.getUniqueId(), this.config.get(Key.MAX_SCORES_PER_PLAYER));
    }

    public JumpPlayer loadPlayer(UUID playerId, int limite) throws SQLException {
        String query = limite > 0 ? SELECT_SCORES:  SELECT_ALL_SCORES;
        @Cleanup PreparedStatement preparedStatement = this.connection.prepareStatement(query);
        preparedStatement.setString(1, playerId.toString());
        if (limite > 0 ) preparedStatement.setInt(2, limite);

        @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
        JumpPlayer jumpPlayer = new JumpPlayer(playerId);

        while (resultSet.next()) {
            String jumpName = resultSet.getString("jump_name");
            long duration = resultSet.getLong("duration");
            this.plugin.getJumpManager().getJump(jumpName).ifPresent(jump -> jumpPlayer.put(jump, duration));
        }

        return jumpPlayer;
    }

    @Override
    public List<JumpPlayer> loadAllPlayers() {
        // fetch all player uuid from score table
        // load each player
        throw new NotImplementedException("Load all players from the database");
    }

    @Override
    public void storePlayer(JumpPlayer jumpPlayer) throws SQLException {
        @Cleanup PreparedStatement preparedStatement = this.connection.prepareStatement(UPSERT_SCORES);

        ImmutableList<ImmutablePair<TimeScore, Jump>> collect = BiStream.from(jumpPlayer)
                .flatMap((jump, timeScores) -> BiStream.from(timeScores, Function.identity(), ignored -> jump))
                .mapValues(ImmutablePair::new)
                .values()
                .collect(ImmutableList.toImmutableList());

        for (ImmutablePair<TimeScore, Jump> pair : collect) {
            writeScore(pair.right, jumpPlayer.getId(), pair.left.getDuration(), preparedStatement);
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public boolean deletePlayer(UUID id) throws SQLException {
        @Cleanup PreparedStatement preparedStatement = this.connection.prepareStatement(DELETE_PLAYER);
        preparedStatement.setString(1, id.toString());
        return preparedStatement.executeUpdate() > 0;
    }

    @Override
    public List<Jump> loadJumps() throws SQLException {
        @Cleanup Statement statement = connection.createStatement();
        @Cleanup ResultSet resultSet = statement.executeQuery(SELECT_JUMPS);

        List<Jump> jumps = new ArrayList<>();
        this.ids.clear();

        while (resultSet.next()) {
            long id = resultSet.getLong("id");
            Jump jump = readJump(resultSet, id);
            jumps.add(jump);
            this.ids.put(jump, id);
        }
        return jumps;
    }

    public boolean jumpExist(Jump jump) throws SQLException {
        Long id = this.ids.get(jump);
        if (id == null) return false;

        @Cleanup PreparedStatement preparedStatement = this.connection.prepareStatement(JUMP_EXISTS);
        preparedStatement.setLong(1, id);
        return preparedStatement.execute();
    }

    @Override
    public void storeJump(Jump jump) throws Exception {
        if (this.jumpExist(jump)) {
            @Cleanup PreparedStatement preparedStatement = this.connection.prepareStatement(UPDATE_JUMP);
            preparedStatement.setLong(3, this.ids.get(jump));
            writeJump(preparedStatement, jump);
            preparedStatement.executeUpdate();

            this.updateLocations(jump);
            this.updateCheckpoints(jump);
        } else this.createJump(jump);
    }

    public void createJump(Jump jump) throws SQLException {
        // insert jump
        @Cleanup
        PreparedStatement preparedStatement = this.connection.prepareStatement(INSERT_JUMP, RETURN_GENERATED_KEYS);
        writeJump(preparedStatement, jump);
        preparedStatement.execute();

        // get id
        @Cleanup ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        generatedKeys.next();
        long id = generatedKeys.getLong(1);
        this.ids.put(jump, id);

        // insert location
        this.updateLocations(jump);
        this.updateCheckpoints(jump);
    }

    private void updateLocations(Jump jump) throws SQLException {
        long id = this.ids.get(jump);

        this.updateLocation("spawn", id, jump::getSpawn);
        this.updateLocation("start", id, jump::getStart);
        this.updateLocation("end", id, jump::getEnd);
    }

    private void updateLocation(String field, long id, Supplier<Optional<Location>> locationSupplier) throws SQLException {
        Optional<Location> location = locationSupplier.get();
        @Cleanup PreparedStatement preparedStatement = this.connection.prepareStatement(String.format(UPDATE_JUMP_LOCATION, field));

        if (location.isPresent()) preparedStatement.setLong(1, this.insertLocation(location.get()));
        else preparedStatement.setNull(1, Types.BIGINT);

        preparedStatement.setLong(2, id);
        preparedStatement.executeUpdate();
    }

    private void updateCheckpoints(Jump jump) throws SQLException {
        long id = this.ids.get(jump);
        List<Location> databaseCheckpoints = this.extractCheckpoints(id);
        List<Location> checkpoints = jump.getCheckpoints();

        List<Location> checkpointsToAdd = new ArrayList<>(checkpoints);
        List<Location> checkpointsToRemove = new ArrayList<>(databaseCheckpoints);
        checkpointsToAdd.removeIf(databaseCheckpoints::contains);
        checkpointsToRemove.removeIf(checkpoints::contains);


        @Cleanup PreparedStatement insert = this.connection.prepareStatement(INSERT_CHECKPOINT);
        @Cleanup PreparedStatement delete = this.connection.prepareStatement(DELETE_CHECKPOINT);

        for (Location location: checkpointsToAdd) {
            int locationId = this.insertLocation(location);
            insert.setLong(1, id);
            insert.setInt(2, locationId);
            insert.executeUpdate();
        }

        for (Location location: checkpointsToRemove) {
            delete.setLong(1, id);
            delete.setInt(2, location.hashCode());
        }
    }

    @Override
    public boolean deleteJump(@NonNull UUID id) throws Exception {
        @Cleanup PreparedStatement preparedStatement = this.connection.prepareStatement(DELETE_JUMP);
        preparedStatement.setBytes(1, UUIDUtils.toBytes(id));
        return preparedStatement.executeUpdate() > 0;
    }

    private Location fetchLocation(int hashCode) throws SQLException {
        if (hashCode == 0) return null; // 0 stands for null value

        @Cleanup PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT world, x, y, z, pitch, yaw FROM `jump_location` WHERE hash = ?");
        preparedStatement.setLong(1, hashCode);
        @Cleanup ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) return readLocation(resultSet);
        else return null;
    }

    private int insertLocation(Location location) throws SQLException {
        int hashCode = location.hashCode();
        @Cleanup PreparedStatement preparedStatement = this.connection.prepareStatement(UPSERT_LOCATION);
        writeLocation(location, hashCode, preparedStatement);
        preparedStatement.executeUpdate();
        preparedStatement.close();
        return hashCode;
    }

    private void cleanupLocations() throws SQLException {
        @Cleanup Statement statement = this.connection.createStatement();
        int deletedLocations = statement.executeUpdate(DELETE_UNUSED_LOCATIONS);
        if (deletedLocations > 0) {
            this.plugin.getLogger().info(String.format("Cleaned up %s locations from the database", deletedLocations));
        }
    }

    private List<Location> extractCheckpoints(long jumpId) throws SQLException {
        @Cleanup PreparedStatement preparedStatement = this.connection.prepareStatement(
                "SELECT world, x, y, z, pitch, yaw " +
                        "FROM `jump_location` l " +
                        "INNER JOIN `jump_checkpoints` c " +
                        "ON c.`location` = l.`hash`" +
                        "WHERE c.`jump` = ?"
        );
        preparedStatement.setLong(1, jumpId);
        @Cleanup ResultSet resultSet = preparedStatement.executeQuery();

        HashSet<Location> locations = new HashSet<>();

        while (resultSet.next()) locations.add(readLocation(resultSet));
        return new ArrayList<>(locations);
    }

    private List<PlayerScore> extractScores(long jumpId) throws SQLException {
        @Cleanup PreparedStatement preparedStatement = this.connection.prepareStatement(
                "SELECT HEX(`player`) `player`, `duration` " +
                        "FROM jump_score " +
                        "WHERE `jump_id` = ? " +
                        "ORDER BY `duration` DESC " +
                        "LIMIT " + this.plugin.getConfigProvider().get(Key.MAX_SCORES_PER_JUMP)
        );

        preparedStatement.setLong(1, jumpId);
        @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
        List<PlayerScore> scores = new ArrayList<>();

        while (resultSet.next()) {

            scores.add(readPlayerScore(resultSet));
        }

        return scores;
    }

    private static void writeScore(Jump jump, UUID playerId, long score, PreparedStatement preparedStatement)
            throws SQLException {
        preparedStatement.setBytes(1, UUIDUtils.toBytes(playerId));
        preparedStatement.setLong(2, score);
        preparedStatement.setString(3, jump.getName());
    }

    private static void writeJump(PreparedStatement preparedStatement, Jump jump) throws SQLException {
        preparedStatement.setString(1, jump.getName());
        preparedStatement.setString(2, jump.getDescription().orElse(null));
        preparedStatement.setString(3, gson.toJson(jump.getItem().serialize()));
        preparedStatement.setBytes( 4, UUIDUtils.toBytes(jump.getId()));
    }

    private static void writeLocation(Location location, int hashCode, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, location.getWorld().getName());
        preparedStatement.setDouble(2, location.getX());
        preparedStatement.setDouble(3, location.getY());
        preparedStatement.setDouble(4, location.getZ());
        preparedStatement.setFloat(5, location.getPitch());
        preparedStatement.setFloat(6, location.getYaw());
        preparedStatement.setInt(7, hashCode);
    }

    private Jump readJump(ResultSet resultSet, long id) throws SQLException {
        return new Jump(
                UUIDUtils.fromBytes(resultSet.getBytes("id")),
                resultSet.getString("name"),
                resultSet.getString("description"),
                this.fetchLocation(resultSet.getInt("spawn")),
                this.fetchLocation(resultSet.getInt("start")),
                this.fetchLocation(resultSet.getInt("end")),
                this.extractCheckpoints(id),
                this.extractScores(id),
                readItem(resultSet)
        );
    }

    private static ItemStack readItem(ResultSet resultSet) throws SQLException {
        String json = resultSet.getString("item");
        Map<String, Object> serializedItem = BiStream.from((Map<?, ?>) gson.fromJson(json, Map.class))
                .mapKeys(Object::toString)
                .mapValues(o -> (Object) o)
                .toMap();
        return ItemStack.deserialize(serializedItem);
    }

    private static Location readLocation(ResultSet resultSet) throws SQLException {
        return new Location(
                Bukkit.getWorld(resultSet.getString("world")),
                resultSet.getDouble("x"),
                resultSet.getDouble("y"),
                resultSet.getDouble("z"),
                resultSet.getFloat("yaw"),
                resultSet.getFloat("pitch")
        );
    }

    private static PlayerScore readPlayerScore(ResultSet resultSet) throws SQLException {
        UUID uuid = UUIDUtils.fromBytes(resultSet.getBytes("player"));
        return new PlayerScore(Bukkit.getOfflinePlayer(uuid), resultSet.getLong("duration"));
    }
}
