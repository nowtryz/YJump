package fr.ycraft.jump.manager.database;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.manager.PlayerManager;
import fr.ycraft.jump.util.DatabaseUtil;
import lombok.Cleanup;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DBPlayerManager extends PlayerManager {
    private final Connection connection;

    public DBPlayerManager(JumpPlugin plugin) throws SQLException {
        super(plugin);
        this.connection = DatabaseUtil.createConnection(plugin.getConfigProvider());
    }

    @Override
    public List<Long> getScores(Player player, Jump jump) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
                "SELECT `duration` " +
                        "FROM `jump_score` s " +
                        "INNER JOIN `jump_jump` j " +
                        "ON s.`jump_id` = j.`id` " +
                        "WHERE j.`name` = ? AND s.`player` = UNHEX(REPLACE(?, '-','')) " +
                        "ORDER BY `duration` DESC " +
                        "LIMIT " + this.plugin.getConfigProvider().getMaxScoresPerPlayer()
        )) {
            preparedStatement.setString(1, jump.getName());
            preparedStatement.setString(2, player.getUniqueId().toString());
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
            List<Long> scores = new ArrayList<>();

            while (resultSet.next()) scores.add(resultSet.getLong(1));
            return scores;
        } catch (SQLException exception) {
            this.plugin.getLogger().severe(String.format(
                    "Unable to retrieve player scores for %s: [%d] %s",
                    player.getName(),
                    exception.getErrorCode(),
                    exception.getLocalizedMessage()
            ));
            return new LinkedList<>();
        }
    }

    @Override
    public void addNewPlayerScore(Player player, Jump jump, long millis) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(
                "INSERT INTO `jump_score`(player, duration, jump_id) " +
                        "VALUE (" +
                        "UNHEX(REPLACE(?, '-','')),?," +
                        "(SELECT `jump_id` FROM `jump_jump` WHERE `name` = ?)" +
                    ")")) {

            writeScore(jump, player, millis, preparedStatement);
            preparedStatement.executeUpdate();

        } catch (SQLException exception) {
            this.plugin.getLogger().severe(String.format(
                    "Unable to save a new score for %s: [%d] %s",
                    player.getName(),
                    exception.getErrorCode(),
                    exception.getLocalizedMessage()
            ));
        }
        // To convert uuid to binary : UNHEX(REPLACE("3f06af63-a93c-11e4-9797-00505690773f", "-",""))
    }

    private static void writeScore(Jump jump, Player player, long score, PreparedStatement preparedStatement)
            throws SQLException {
        preparedStatement.setString(1, player.getUniqueId().toString());
        preparedStatement.setLong(2, score);
        preparedStatement.setString(3, jump.getName());
    }
}
