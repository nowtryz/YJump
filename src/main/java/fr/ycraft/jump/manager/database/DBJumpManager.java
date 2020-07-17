package fr.ycraft.jump.manager.database;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.manager.JumpManager;
import fr.ycraft.jump.util.DatabaseUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class DBJumpManager extends JumpManager {
    private final Connection connection;
    private final Map<Jump, Long> ids = new ConcurrentHashMap<>();

    public DBJumpManager(JumpPlugin plugin) throws SQLException {
        super(plugin);
        this.connection = DatabaseUtil.initDatabase(plugin);
        this.jumps = new ConcurrentHashMap<>();

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT id, name, description, spawn, start, end FROM `jump_jump`");

        while (resultSet.next()) {
            Jump jump = new Jump(
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    this.extractLocation(resultSet.getLong("spawn")),
                    this.extractLocation(resultSet.getLong("start")),
                    this.extractLocation(resultSet.getLong("end")),
                    null,
                    new LinkedList<>(),
                    new ItemStack(Material.SLIME_BLOCK)
            );
            this.jumps.put(jump.getName(), jump);
            this.ids.put(jump, resultSet.getLong("id"));
        }

        this.updateJumpList();
    }

    @Override
    public void persist(Jump jump) {
        try {
            if (this.jumps.containsValue(jump)) {
                // update
                PreparedStatement preparedStatement = this.connection.prepareStatement("UPDATE `jump_jump` SET description = ? WHERE id = ?");
                preparedStatement.setString(1, jump.getDescription().orElse(null));
                preparedStatement.setLong(2, this.ids.get(jump));
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } else {
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
            }
            this.updateLocations(jump);
            this.updateJumpList();
        } catch (SQLException exception) {
            exception.printStackTrace(); // TODO logger
        }
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
                exception.printStackTrace(); // TODO logger
            }
        });
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

    private Location extractLocation(long id) throws SQLException {
        if (id == 0) return null; // 0 stands for null value

        PreparedStatement preparedStatement = this.connection.prepareStatement("SELECT world, x, y, z, pitch, yaw FROM `jump_location` WHERE id = ?");
        preparedStatement.setLong(1, id);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) return new Location(
                Bukkit.getWorld(resultSet.getString("world")),
                resultSet.getDouble("x"),
                resultSet.getDouble("y"),
                resultSet.getDouble("z"),
                resultSet.getFloat("pitch"),
                resultSet.getFloat("yaw")
        );
        else return null;
    }

    private long insertLocation(Location location) throws SQLException {
        PreparedStatement preparedStatement = this.connection.prepareStatement(
                "INSERT INTO `jump_location` (world,x,y,z,pitch,yaw) VALUE (?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS
        );

        preparedStatement.setString(1, location.getWorld().getName());
        preparedStatement.setDouble(2, location.getBlockX() + 0.5);
        preparedStatement.setDouble(3, location.getBlockY());
        preparedStatement.setDouble(4, location.getBlockZ() + 0.5);
        preparedStatement.setFloat(5, location.getPitch());
        preparedStatement.setFloat(6, location.getYaw());
        preparedStatement.executeUpdate();

        ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        generatedKeys.next();
        long id = generatedKeys.getLong(1);
        generatedKeys.close();
        preparedStatement.close();

        return id;
    }
}
