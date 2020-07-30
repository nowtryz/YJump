package fr.ycraft.jump.manager.database;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.PlayerScore;
import fr.ycraft.jump.manager.JumpManager;
import fr.ycraft.jump.util.DatabaseUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

// TODO test scores when finished

public class DBJumpManager extends JumpManager {
    private final Connection connection;
    private final Map<Jump, Long> ids = new ConcurrentHashMap<>();

    public DBJumpManager(JumpPlugin plugin) throws SQLException {
        super(plugin);
        this.connection = DatabaseUtil.createConnection(plugin.getConfigProvider());
        this.jumps = new ConcurrentHashMap<>();

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT id, name, description, spawn, start, end FROM `jump_jump`");

        while (resultSet.next()) {
            long id = resultSet.getLong("id");
            Jump jump = new Jump(
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    this.extractLocation(resultSet.getInt("spawn")),
                    this.extractLocation(resultSet.getInt("start")),
                    this.extractLocation(resultSet.getInt("end")),
                    this.extractCheckpoints(id),
                    this.extractScores(id),
                    new ItemStack(Material.SLIME_BLOCK)
            );
            this.jumps.put(jump.getName(), jump);
            this.ids.put(jump, id);
        }

        this.updateJumpList();
    }

    @Override
    public void persist(Jump jump) {
        if (this.jumps.containsValue(jump)) {
            try {
                // update
                PreparedStatement preparedStatement = this.connection.prepareStatement("UPDATE `jump_jump` SET description = ? WHERE id = ?");
                preparedStatement.setString(1, jump.getDescription().orElse(null));
                preparedStatement.setLong(2, this.ids.get(jump));
                preparedStatement.executeUpdate();
                preparedStatement.close();
                this.updateLocations(jump);
                this.updateCheckpoints(jump);
            } catch (SQLException exception) {
                this.plugin.getLogger().severe(String.format(
                        "Unable to update jump named %s in the database: [%s] %s",
                        jump.getName(),
                        exception.getSQLState(),
                        exception.getLocalizedMessage()
                ));
                exception.printStackTrace();
            }
        } else {
            try {
                // insert
                PreparedStatement preparedStatement = this.connection.prepareStatement(
                        "INSERT INTO `jump_jump`(name, description) VALUE (?,?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                preparedStatement.setString(1, jump.getName());
                preparedStatement.setString(2, jump.getDescription().orElse(null));
                preparedStatement.execute();

                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                generatedKeys.next();
                long id = generatedKeys.getLong(1);
                generatedKeys.close();
                preparedStatement.close();

                this.jumps.put(jump.getName(), jump);
                this.ids.put(jump, id);
                this.updateLocations(jump);
                this.updateCheckpoints(jump);
            } catch (SQLException exception) {
                this.plugin.getLogger().severe(String.format(
                        "Unable to insert jump named %s in the database: [%s] %s",
                        jump.getName(),
                        exception.getSQLState(),
                        exception.getLocalizedMessage()
                ));
            }
        }
        this.updateJumpList();
    }

    @Override
    public void updateName(Jump jump, String name) {
        String oldName = jump.getName();
        Optional.ofNullable(this.ids.get(jump)).ifPresent(id -> {
            try {
                PreparedStatement preparedStatement = this.connection.prepareStatement("UPDATE `jump_jump` SET name = ? WHERE id = ?");
                preparedStatement.setString(1, name);
                preparedStatement.setLong(2, id);
                preparedStatement.executeUpdate();
                preparedStatement.close();

                this.ids.remove(jump, id);
                jump.setName(name);
                this.jumps.remove(oldName);
                this.jumps.put(name, jump);
                this.ids.put(jump, id);
            } catch (SQLException exception) {
                this.plugin.getLogger().severe(String.format(
                        "Unable to update jump named %s in the database: [%d]%s",
                        jump.getName(),
                        exception.getErrorCode(),
                        exception.getLocalizedMessage()
                ));
            }
        });
    }

    @Override
    public void deleteCheckpoint(Jump jump, Location location) {
        try {
            PreparedStatement preparedStatement = this.connection
                    .prepareStatement("DELETE FROM `jump_checkpoints` WHERE `location` = ?");
            preparedStatement.setInt(1, location.hashCode());
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            this.plugin.getLogger().severe(String.format(
                    "Unable to remove %s's checkpoint (@%d, %d, %d) from the database: [%d]%s",
                    jump.getName(),
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ(),
                    exception.getErrorCode(),
                    exception.getLocalizedMessage()
            ));
        }
    }

    @Override
    public void delete(Jump jump) {
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement("DELETE FROM `jump_jump` WHERE name = ?");
            preparedStatement.setString(1, jump.getName());
            preparedStatement.executeUpdate();
            preparedStatement.close();
            this.jumps.remove(jump.getName(), jump);
            this.ids.remove(jump);
        } catch (SQLException exception) {
            exception.printStackTrace(); // TODO logger
        }
    }

    @Override
    public void save() {
        this.jumps.values().forEach(this::persist);
        try {
            this.connection.close();
        } catch (SQLException exception) {
            this.plugin.getLogger().severe(String.format(
                    "Unable to close database connection: [%d]%s",
                    exception.getErrorCode(),
                    exception.getLocalizedMessage()
            ));
        }

    }

    private void updateLocations(Jump jump) throws SQLException {
        long id = this.ids.get(jump);

        this.updateLocation("spawn", id, jump::getSpawn);
        this.updateLocation("start", id, jump::getStart);
        this.updateLocation("end", id, jump::getEnd);
    }

    private void updateLocation(String field, long id, Supplier<Optional<Location>> locationSupplier) throws SQLException {
        // FIXME Update only if necessary

        Optional<Location> location = locationSupplier.get();
        PreparedStatement preparedStatement = this.connection.prepareStatement("UPDATE `jump_jump` SET " + field + " = ? WHERE id = ?");

        if (location.isPresent()) preparedStatement.setLong(1, this.insertLocation(location.get()));
        else preparedStatement.setNull(1, Types.BIGINT);

        preparedStatement.setLong(2, id);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    private void updateCheckpoints(Jump jump) throws SQLException {
        long id = this.ids.get(jump);

        PreparedStatement preparedStatement = this.connection.prepareStatement(
                "INSERT IGNORE INTO `jump_checkpoints` (`jump`, `location`) VALUE (?,?)");

        for (Location location: jump.getCheckpoints()) {
            preparedStatement.setLong(1, id);
            preparedStatement.setInt(2, this.insertLocation(location));
            preparedStatement.executeUpdate();
        }

        preparedStatement.close();
    }

    private List<PlayerScore> extractScores(long jumpId) throws SQLException {
        PreparedStatement preparedStatement = this.connection.prepareStatement(
                "SELECT HEX(`player`) `player`, `duration` " +
                "FROM jump_score " +
                "WHERE `jump_id` = ? " +
                "ORDER BY `duration` DESC " +
                "LIMIT " + this.plugin.getConfigProvider().getMaxScoresPerJump()
        );

        preparedStatement.setLong(1, jumpId);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<PlayerScore> scores = new ArrayList<>();

        while (resultSet.next()) {
            UUID uuid = UUID.fromString(resultSet.getString(1).replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
            ));

            scores.add(new PlayerScore(
                    Bukkit.getOfflinePlayer(uuid),
                    resultSet.getLong(2)
            ));
        }

        resultSet.close();
        preparedStatement.close();
        return scores;
    }

    private List<Location> extractCheckpoints(long jumpId) throws SQLException {
        PreparedStatement preparedStatement = this.connection.prepareStatement(
            "SELECT world, x, y, z, pitch, yaw " +
            "FROM `jump_location` l " +
            "INNER JOIN `jump_checkpoints` c " +
            "ON c.`location` = l.`hash`" +
            "WHERE c.`jump` = ?"
        );
        preparedStatement.setLong(1, jumpId);
        ResultSet resultSet = preparedStatement.executeQuery();

        HashSet<Location> locations = new HashSet<>();

        while (resultSet.next()) locations.add(this.extractLocationFromResultSet(resultSet));
        return new ArrayList<>(locations);
    }

    private Location extractLocation(int hashCode) throws SQLException {
        if (hashCode == 0) return null; // 0 stands for null value

        PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT world, x, y, z, pitch, yaw FROM `jump_location` WHERE hash = ?");
        preparedStatement.setLong(1, hashCode);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) return this.extractLocationFromResultSet(resultSet);
        else return null;
    }

    private Location extractLocationFromResultSet(ResultSet resultSet) throws SQLException {
        return new Location(
                Bukkit.getWorld(resultSet.getString("world")),
                resultSet.getDouble("x"),
                resultSet.getDouble("y"),
                resultSet.getDouble("z"),
                resultSet.getFloat("yaw"),
                resultSet.getFloat("pitch")
        );
    }

    private int insertLocation(Location location) throws SQLException {
        int hashCode = location.hashCode();
        PreparedStatement preparedStatement = this.connection.prepareStatement(
                "INSERT IGNORE INTO `jump_location` (world,x,y,z,pitch,yaw,hash) VALUE (?,?,?,?,?,?,?)"
        );

        this.setLocationValues(location, hashCode, preparedStatement);
        preparedStatement.executeUpdate();
        preparedStatement.close();

        return hashCode;
    }

    private void setLocationValues(Location location, int hashCode, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, location.getWorld().getName());
        preparedStatement.setDouble(2, location.getBlockX() + 0.5);
        preparedStatement.setDouble(3, location.getBlockY());
        preparedStatement.setDouble(4, location.getBlockZ() + 0.5);
        preparedStatement.setFloat(5, location.getPitch());
        preparedStatement.setFloat(6, location.getYaw());
        preparedStatement.setInt(7, hashCode);
    }
}
