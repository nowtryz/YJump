package fr.ycraft.jump.manager;

import fr.ycraft.jump.JumpGame;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class GameManager extends AbstractManager {
    private final Map<Player, JumpGame> runningGames = new LinkedHashMap<>();

    public GameManager(JumpPlugin plugin) {
        super(plugin);
    }

    public boolean isPlaying(Player player) {
        return this.runningGames.containsKey(player);
    }

    public Optional<JumpGame> getGame(Player player) {
        return Optional.ofNullable(this.runningGames.get(player));
    }

    public void enter(Player player, Jump jump) {
        JumpGame game = new JumpGame(this.plugin, jump, player);
        this.runningGames.put(player, game);
    }

    public Collection<JumpGame> getGames() {
        return this.runningGames.values();
    }

    public void remove(Player player, JumpGame game) {
        this.runningGames.remove(player, game);
    }

    public void stopAll() {
        this.runningGames.values().forEach(JumpGame::stop);
        this.runningGames.clear();
    }
}
