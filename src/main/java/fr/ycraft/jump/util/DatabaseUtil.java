package fr.ycraft.jump.util;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Config;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class DatabaseUtil {
    public static Connection initDatabase(JumpPlugin plugin) throws SQLException {
        Connection connection = connect(plugin.getConfigProvider());
        Statement statement = connection.createStatement();
        Logger logger = plugin.getLogger();

        ResultSet result = connection.getMetaData().getTables(null, null, null, null);
        Set<String> tables = new HashSet<>();

        while (result.next()) tables.add(result.getString("TABLE_NAME"));
        result.close();

        if (!tables.contains("jump_location")) {
            createLocationTable(statement);
            logger.info("Created Location table");
        }

        if (!tables.contains("jump_jump")) {
            createJumpTable(statement);
            logger.info("Created Jump table");
        }

        if (!tables.contains("jump_score")) {
            createScoreTable(statement);
            logger.info("Created Score table");
        }

        return connection;
    }

    private static Connection connect(Config config) throws SQLException {
        return DriverManager.getConnection(
                String.format(
                        "jdbc:mysql://%s:%d/%s",
                        config.getDatabaseHost(),
                        config.getDatabasePort(),
                        config.getDatabaseName()
                ),
                config.getDatabaseUser(),
                config.getDatabasePassword()
        );
    }

    private static void createLocationTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS `jump_location` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT , " +
                "`world` VARCHAR(50) NOT NULL , " +
                "`x` DOUBLE NOT NULL , " +
                "`y` DOUBLE NOT NULL , " +
                "`z` DOUBLE NOT NULL , " +
                "`pitch` FLOAT NOT NULL , " +
                "`yaw` FLOAT NOT NULL , " +
                "PRIMARY KEY (`id`)" +
                ") ENGINE = InnoDB CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;");
    }

    private static void createJumpTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS `jump_jump` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT , " +
                "`name` VARCHAR(16) NOT NULL, " +
                "`description` TEXT," +
                "`spawn` BIGINT ," +
                "`start` BIGINT ," +
                "`end` BIGINT ," +
                "`item` TEXT ," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE (`name`)," +
                "FOREIGN KEY (`spawn`) " +
                    "REFERENCES `jump_location`(`id`) " +
                    "ON DELETE CASCADE , " +
                "FOREIGN KEY (`start`) " +
                    "REFERENCES `jump_location`(`id`) " +
                    "ON DELETE CASCADE , " +
                "FOREIGN KEY (`end`) " +
                    "REFERENCES `jump_location`(`id`) " +
                    "ON DELETE CASCADE " +
                ") ENGINE = InnoDB CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci");
    }

    private static void createScoreTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE `jump_score` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT , " +
                "`player` BINARY(16) NOT NULL , " +
                "`duration` BIGINT NOT NULL , " +
                "`jump_id` BIGINT NOT NULL , " +
                "PRIMARY KEY (`id`) , " +
                "FOREIGN KEY (`jump_id`) " +
                    "REFERENCES `jump_jump`(`id`) " +
                    "ON DELETE CASCADE" +
                ") ENGINE = InnoDB CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;");
    }
}
