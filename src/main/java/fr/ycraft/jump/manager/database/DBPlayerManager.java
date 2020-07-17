package fr.ycraft.jump.manager.database;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.manager.PlayerManager;
import fr.ycraft.jump.util.DatabaseUtil;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class DBPlayerManager extends PlayerManager {
    private final Connection connection;

    public DBPlayerManager(JumpPlugin plugin) throws SQLException {
        super(plugin);
        this.connection = DatabaseUtil.initDatabase(plugin);
    }

    @Override
    public List<Long> getScores(Player player, Jump jump) {
        return new LinkedList<>();
    }

    @Override
    public void addNewPlayerScore(Player player, Jump jump, long millis) {

    }
}
