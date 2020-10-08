package fr.ycraft.jump.manager;

import com.google.inject.Inject;
import com.google.inject.Injector;
import fr.ycraft.jump.JumpGame;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.listeners.GameListener;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

@Singleton
public class GameManager extends AbstractManager {
    private final Map<Player, JumpGame> runningGames = new LinkedHashMap<>();
    private final Injector injector;
    @Setter private GameListener listener;

    @Inject
    public GameManager(JumpPlugin plugin, Injector injector) {
        super(plugin);
        this.injector = injector;
    }

    public boolean isPlaying(Player player) {
        return this.runningGames.containsKey(player);
    }

    public Optional<JumpGame> getGame(Player player) {
        return Optional.ofNullable(this.runningGames.get(player));
    }

    public void enter(Player player, Jump jump) {
        if (this.runningGames.isEmpty()) this.listener.register();
        this.runningGames.put(player, null);

        Bukkit.getScheduler().runTask(this.plugin, () -> this.initializeGame(player, jump));
    }

    private synchronized void initializeGame(Player player, Jump jump) {
        try {
            JumpGame game = new JumpGame(this.plugin, jump, player);
            this.injector.injectMembers(game);
            this.runningGames.put(player, game);

            Bukkit.getScheduler().runTask(this.plugin, game::init);
        } catch (Exception exception) {
            this.plugin.getLogger().log(
                    Level.SEVERE,
                    "Unable to create game session for " + player.getName() + ": " + exception.getMessage(),
                    exception
            );

        }
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
