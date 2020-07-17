package fr.ycraft.jump.manager.file;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.manager.PlayerManager;
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

public class FilePlayerManager extends PlayerManager implements Listener {
    private final File scoresFolder;
    private final Map<Player, Map<String, List<Long>>> playerScores = new HashMap<>();
    private final Map<Player, ReentrantLock> locks = new HashMap<>();

    public FilePlayerManager(JumpPlugin plugin) {
        super(plugin);
        this.scoresFolder = new File(plugin.getDataFolder(), "players");
        if (this.scoresFolder.mkdirs()) plugin.getLogger().info("Created a fresh new score folder");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> this.loadScores(event.getPlayer()));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> this.saveScores(event.getPlayer()));
    }

    @Override
    public List<Long> getScores(Player player, Jump jump) {
        return Collections.unmodifiableList(this.getScoreSection(player, jump));
    }

    private ReentrantLock addLock(Player player) {
        ReentrantLock lock = new ReentrantLock();
        this.locks.put(player, lock);
        return lock;
    }

    private Map<String, List<Long>> getJumps(Player player) {
        Map<String, List<Long>> scores = this.playerScores.get(player);
        if (scores == null) scores = this.loadScores(player);
        return scores;
    }

    private List<Long> getScoreSection(Player player, Jump jump) {

        List<Long> list = this.getJumps(player).get(jump.getName());
        if (list == null) list = new ArrayList<>();

        return list;
    }

    @Override
    public void addNewPlayerScore(Player player, Jump jump, long millis) {
        int maxScoresPerPlayer = this.plugin.getConfigProvider().getMaxScoresPerPlayer();
        ReentrantLock lock = Optional.ofNullable(this.locks.get(player)).orElseGet(() -> this.addLock(player));
        lock.lock();

        List<Long> scores = this.getScoreSection(player, jump);

        if (scores.size() < maxScoresPerPlayer -1
                || scores.stream().anyMatch(score -> score < millis)) {
            scores.add(millis);
            scores.sort(Long::compareTo);

            if (scores.size() > maxScoresPerPlayer) {
                List<Long> newScores = scores.subList(0, maxScoresPerPlayer);
                this.playerScores.get(player).put(jump.getName(), newScores);
            }
        }

        lock.unlock();
        this.saveScores(player);
    }

    protected Map<String, List<Long>> loadScores(Player player) {
        ReentrantLock lock = Optional.ofNullable(this.locks.get(player)).orElseGet(() -> this.addLock(player));
        lock.lock();

        File file = this.getFile(player);

        try {
            if (!file.exists() && file.createNewFile()) {
                this.plugin.getLogger().info(String.format("Created a new score file for %s", player.getName()));
            }

            YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
            this.playerScores.put(player, this.plugin.getJumpManager()
                    .getJumps()
                    .keySet()
                    .stream()
                    .collect(Collectors.toMap(Function.identity(),
                            j -> Optional.ofNullable(conf.getLongList(j)).orElseGet(ArrayList::new))));

        } catch (IOException exception) {
            plugin.getLogger().log(Level.SEVERE, "Unable to load scores file", exception);
            throw new RuntimeException("Unable to process score pipeline", exception);
        } finally {
            lock.unlock();
        }


        return this.playerScores.get(player);
    }

    protected void saveScores(Player player) {
        ReentrantLock lock = Optional.ofNullable(this.locks.get(player)).orElseGet(() -> this.addLock(player));
        lock.lock();

        try {
            File file = this.getFile(player);

            YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
            this.getJumps(player).forEach(conf::set);
            conf.save(file);
        } catch (IOException exception) {
            this.plugin.getLogger().log(Level.SEVERE, "Unable to save scores", exception);
        } finally {
            lock.unlock();
        }
    }

    private File getFile(Player player) {
        return new File(this.scoresFolder, player.getUniqueId() + ".yml");
    }
}
