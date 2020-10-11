package fr.ycraft.jump.manager;

import com.google.inject.Inject;
import fr.ycraft.jump.factories.JumpGameFactory;
import fr.ycraft.jump.JumpGame;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.listeners.GameListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class GameManager extends AbstractManager {
    private final Map<Player, JumpGame> runningGames = new LinkedHashMap<>();
    private final JumpGameFactory factory;
    private final GameListener listener;

    @Inject
    public GameManager(JumpPlugin plugin, GameListener listener, JumpGameFactory factory) {
        super(plugin);
        this.factory = factory;
        this.listener = listener;
        this.listener.setGameManager(this);
    }

    public boolean isPlaying(Player player) {
        return this.runningGames.containsKey(player);
    }

    public Optional<JumpGame> getGame(Player player) {
        return Optional.ofNullable(this.runningGames.get(player));
    }

    public synchronized void enter(Player player, Jump jump) {
        if (this.runningGames.isEmpty()) this.listener.register();

        JumpGame game = this.factory.create(jump, player);
        this.runningGames.put(player, game);

        Bukkit.getScheduler().runTask(this.plugin, game::init);
    }

    public Collection<JumpGame> getGames() {
        return this.runningGames.values();
    }

    public void remove(Player player, JumpGame game) {
        this.runningGames.remove(player, game);
        if (this.runningGames.isEmpty()) this.listener.unRegister();
    }

    public void stopAll() {
        this.runningGames.values().forEach(JumpGame::stop);
        this.runningGames.clear();
    }
}
