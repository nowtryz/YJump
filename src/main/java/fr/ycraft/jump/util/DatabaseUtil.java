package fr.ycraft.jump.util;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class DatabaseUtil {
    public static void initDatabase(JumpPlugin plugin) throws SQLException {
        Connection connection = createConnection(plugin.getConfigProvider());
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

        if (!tables.contains("jump_checkpoints")) {
            createCheckpointTable(statement);
            logger.info("Created Score table");
        }

    }

    public static Connection createConnection(Config config) throws SQLException {
        return DriverManager.getConnection(
                String.format(
                        "jdbc:mysql://%s:%d/%s",
                        config.get(Key.DATABASE_HOST),
                        config.get(Key.DATABASE_PORT),
                        config.get(Key.DATABASE_NAME)
                ),
                config.get(Key.DATABASE_USER),
                config.get(Key.DATABASE_PASSWORD)
        );
    }

    private static void createLocationTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS `jump_location` (" +
                "`hash` INT NOT NULL , " +
                "`world` VARCHAR(50) NOT NULL , " +
                "`x` DOUBLE NOT NULL , " +
                "`y` DOUBLE NOT NULL , " +
                "`z` DOUBLE NOT NULL , " +
                "`pitch` FLOAT NOT NULL , " +
                "`yaw` FLOAT NOT NULL , " +
                "PRIMARY KEY (`hash`)" +
                ") ENGINE = InnoDB CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;");
    }

    private static void createJumpTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS `jump_jump` (" +
                "`id` BINARY(16) NOT NULL , " +
                "`name` VARCHAR(16) NOT NULL, " +
                "`description` TEXT, " +
                "`spawn` INT, " +
                "`start` INT, " +
                "`end` INT, " +
                "`item` TEXT, " +
                "PRIMARY KEY (`id`), " +
                "UNIQUE (`name`)," +
                "FOREIGN KEY (`spawn`) " +
                    "REFERENCES `jump_location`(`hash`) " +
                    "ON DELETE CASCADE , " +
                "FOREIGN KEY (`start`) " +
                    "REFERENCES `jump_location`(`hash`) " +
                    "ON DELETE CASCADE , " +
                "FOREIGN KEY (`end`) " +
                    "REFERENCES `jump_location`(`hash`) " +
                    "ON DELETE CASCADE " +
                ") ENGINE = InnoDB CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci");
    }

    private static void createScoreTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS `jump_score` (" +
                "`id` BIGINT NOT NULL AUTO_INCREMENT , " +
                "`player` BINARY(16) NOT NULL , " +
                "`duration` BIGINT NOT NULL , " +
                "`jump_id` BIGINT NOT NULL , " +
                "PRIMARY KEY (`id`) , " +
                "UNIQUE (`player`, `duration`, `jump_id`) , " +
                "FOREIGN KEY (`jump_id`) " +
                    "REFERENCES `jump_jump`(`id`) " +
                    "ON DELETE CASCADE" +
                ") ENGINE = InnoDB CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci;");
    }

    private static void createCheckpointTable(Statement statement) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS `jump_checkpoints` (" +
                "`jump` BIGINT NOT NULL ," +
                "`location` INT NOT NULL ," +
                "PRIMARY KEY (`jump`, `location`)," +
                "FOREIGN KEY (`jump`)" +
                "    REFERENCES `jump_jump`(`id`)" +
                "    ON DELETE CASCADE ," +
                "FOREIGN KEY (`location`)" +
                "    REFERENCES `jump_location`(`hash`)" +
                "    ON DELETE CASCADE" +
                ") ENGINE = InnoDB CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci");
    }
}
