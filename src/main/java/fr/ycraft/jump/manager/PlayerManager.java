package fr.ycraft.jump.manager;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class PlayerManager extends AbstractManager {
    public PlayerManager(JumpPlugin plugin) {
        super(plugin);
    }

    public abstract List<Long> getScores(Player player, Jump jump);
    public abstract void addNewPlayerScore(Player player, Jump jump, long millis);
}
