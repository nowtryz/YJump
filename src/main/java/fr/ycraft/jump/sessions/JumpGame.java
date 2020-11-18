package fr.ycraft.jump.sessions;

import com.google.inject.assistedinject.Assisted;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.commands.enums.Perm;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.configuration.TitleSettings;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.JumpPlayer;
import fr.ycraft.jump.entity.TimeScore;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.manager.GameManager;
import fr.ycraft.jump.manager.PlayerManager;
import fr.ycraft.jump.storage.Storage;
import lombok.Getter;
import net.nowtryz.mcutils.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class JumpGame {
    private static final int TIMER_HEADER_POS = 5;
    private static final int TIMER_VALUE_POS = 4;
    private static final int CHECKPOINT_HEADER_POS = 2;
    private static final int CHECKPOINT_VALUE_POS = 1;

    private final @Getter(onMethod_={@NotNull}) Jump jump;
    private final @NotNull Location startLocation;
    private final @NotNull Location spawnLocation;
    private final @NotNull Location endLocation;
    private final List<Location> validated = new LinkedList<>();
    private final Config config;
    private final JumpPlugin plugin;
    private final JumpPlayer jumpPlayer;
    private final GameManager gameManager;
    private final Storage storage;
    private final Player player;
    private final TitleSettings startTitle;
    private final TitleSettings resetTitle;
    private final TitleSettings checkpointTitle;
    private final TitleSettings endTitle;
    private final boolean fallPrevention;
    private final boolean canFly;
    private final boolean bossBarEnabled;
    private final boolean sidebarEnabled;
    private BossBar bossBar;
    private Scoreboard originalScoreboard;
    private BukkitTask bukkitTask;
    private Objective objective;
    private Scoreboard scoreboard;
    private Score timer;
    private Score checkpoints;
    private long resetTime;
    private boolean ended = false;
    private boolean wasCollidable = true;
    private long start;
    private Location checkpoint;

    @Inject
    JumpGame(Config config,
             PlayerManager playerManager,
             GameManager gameManager,
             Storage storage,
             JumpPlugin plugin,
             @Assisted Jump jump,
             @Assisted Player player) {

        Optional<JumpPlayer> jumpPlayer = playerManager.getPlayer(player);
        Optional<Location> start = jump.getStart();
        Optional<Location> spawn = jump.getSpawn();
        Optional<Location> end = jump.getEnd();
        assert start.isPresent();
        assert spawn.isPresent();
        assert end.isPresent();
        assert jumpPlayer.isPresent();

        this.jumpPlayer = jumpPlayer.get();
        this.gameManager = gameManager;
        this.storage = storage;
        this.config = config;
        this.resetTime = config.get(Key.RESET_TIME);
        this.bossBarEnabled = config.get(Key.BOSS_BAR_ENABLED);
        this.sidebarEnabled = config.get(Key.SIDEBAR_ENABLED);
        this.startTitle = config.get(Key.START_TITLE);
        this.resetTitle = config.get(Key.RESET_TITLE);
        this.checkpointTitle = config.get(Key.CHECKPOINT_TITLE);
        this.endTitle = config.get(Key.END_TITLE);
        this.plugin = plugin;
        this.jump = jump;
        this.player = player;
        this.startLocation = start.get();
        this.spawnLocation = spawn.get();
        this.endLocation = end.get();
        this.start = System.currentTimeMillis();
        this.resetTime = config.get(Key.RESET_TIME);
        this.canFly = player.hasPermission(Perm.FLY);
        this.fallPrevention = jump.getFallDistance() > 0;
        this.checkpoint = this.startLocation;
    }

    /**
     * Initialize this game session and add information to the player's ATH.
     * This method must be called on the primary thread of Bukkit
     * @throws IllegalStateException if this method is not call on primary thread
     */
    public void init() {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Async Parkour initialization");

        Text.GAME_ENTER.send(player, jump.getName());
        this.startTitle.send(player,
                Text.GAME_ENTER_TITLE.get(this.jump.getName()),
                Text.GAME_ENTER_SUBTITLE.get(this.jump.getName())
        );

        this.wasCollidable = this.player.isCollidable();
        if (this.config.get(Key.DISABLE_COLLISIONS)) this.player.setCollidable(false);
        if (!this.canFly) player.setFlying(false);
        if (this.config.get(Key.RESET_ENCHANTS)) player
                .getActivePotionEffects()
                .stream()
                .map(PotionEffect::getType)
                .forEach(player::removePotionEffect);

        // Init scoreboard
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("Sidebar", "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective.setDisplayName(Text.SCOREBOARD_DISPLAY_NAME.get(this.jump.getName()));
        this.objective.getScore(Text.SCOREBOARD_TIMER_HEADER.get()).setScore(TIMER_HEADER_POS);
        this.objective.getScore(Text.SCOREBOARD_CHECKPOINT_HEADER.get()).setScore(CHECKPOINT_HEADER_POS);
        this.objective.getScore(" ").setScore(3);

        this.timer = this.objective.getScore(Text.SCOREBOARD_TIMER_VALUE.get(0, 0, 0));
        this.timer.setScore(TIMER_VALUE_POS);
        this.checkpoints = this.objective.getScore(Text.SCOREBOARD_CHECKPOINT_VALUE.get(0, this.jump.getCheckpoints().size()));
        this.checkpoints.setScore(CHECKPOINT_VALUE_POS);

        this.originalScoreboard = player.getScoreboard();

        // Init bossbar
        this.bossBar = Bukkit.createBossBar(
                Text.GAME_BOSSBAR.get(jump.getName(), 0, jump.getCheckpoints().size()),
                plugin.getConfigProvider().get(Key.BOSS_BAR_COLOR),
                BarStyle.SOLID
        );
        this.bossBar.setProgress(0);

        // add ATH to player
        if (this.sidebarEnabled) this.player.setScoreboard(scoreboard);
        if (this.bossBarEnabled) this.bossBar.addPlayer(player);

        // setup timer
        if(this.sidebarEnabled) {
            this.bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateTimer, 1, 1);
        }
    }

    /**
     * Method called every tick by bukkit to update the sidebar
     */
    public void updateTimer() {
        TimeScore score = new TimeScore(System.currentTimeMillis() - this.start);

        if (this.sidebarEnabled) {
            this.scoreboard.resetScores(this.timer.getEntry());
            this.timer = this.objective.getScore(score.getText(Text.SCOREBOARD_TIMER_VALUE));
            this.timer.setScore(TIMER_VALUE_POS);
        }
    }

    /**
     * Update the validated checkpoint count on the sidebar
     * @param count the number of checkpoints already validated
     */
    public void updateCheckpointsScoreboard(int count) {
        if (!this.sidebarEnabled) return;

        this.scoreboard.resetScores(this.checkpoints.getEntry());
        this.checkpoints = this.objective.getScore(Text.SCOREBOARD_CHECKPOINT_VALUE.get(count, this.jump.getCheckpoints().size()));
        this.checkpoints.setScore(CHECKPOINT_VALUE_POS);
    }

    /**
     * Notify the player has executed a command. Used by the {@link fr.ycraft.jump.listeners.GameListener GameListener}
     * @see fr.ycraft.jump.listeners.GameListener
     * @param event original event
     */
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String[] command = event.getMessage().substring(1).split(" ");
        if (command.length == 0) return;
        if (this.config.get(Key.ALLOWED_COMMANDS).stream().anyMatch(command[0]::equals)) return;

        this.close();
        Text.LEFT_JUMP_ERROR.send(event.getPlayer(), Text.NO_COMMANDS.get());
    }

    /**
     * Notify the player has tried to toggle fly. Used by the {@link fr.ycraft.jump.listeners.GameListener GameListener}
     * @see fr.ycraft.jump.listeners.GameListener
     * @param event original event
     */
    public void onFly(PlayerToggleFlightEvent event) {
        if (!this.canFly) {
            event.setCancelled(true);
            Text.NO_FLY.send(event.getPlayer());
        }
    }

    /**
     * Notify the player has interacted with a material. This method is able to listen when the player walk on a plate
     * to reset the chrono, validate a checkpoint, or end the parkour.
     * Used by the {@link fr.ycraft.jump.listeners.GameListener GameListener}
     * @see fr.ycraft.jump.listeners.GameListener
     * @param event original event
     */
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.PHYSICAL)
                || event.getClickedBlock() == null
                || !Jump.ALLOWED_MATERIALS.contains(event.getClickedBlock().getType())
        ) {
            event.setCancelled(true);
            return;
        }

        Location loc = event.getClickedBlock().getLocation();

        // jump end
        if (LocationUtil.isBlockLocationEqual(this.endLocation, loc)) {
            if (this.validated.size() == this.jump.getCheckpoints().size()) this.end();
            else Text.GAME_MISSING_CHECKPOINT.send(this.player);
        }
        // chrono reset
        else if (LocationUtil.isBlockLocationEqual(this.startLocation, loc)) {
            long current = System.currentTimeMillis();
            if (current - this.start < this.resetTime) return;
            this.reset(current);
        }
        // Validate checkpoint
        else if (this.validated.stream().noneMatch(l -> LocationUtil.isBlockLocationEqual(l, loc))){
            this.jump.getCheckpoints().stream()
                    .filter(l -> LocationUtil.isBlockLocationEqual(l, loc))
                    .findFirst().ifPresent(this::validateCheckpoint);
        }
    }

    /**
     * Notify the player moved to identity its fall distance.
     * Used by the {@link fr.ycraft.jump.listeners.GameListener GameListener}
     * @see fr.ycraft.jump.listeners.GameListener
     */
    public void onMove() {
        if (this.fallPrevention && this.player.getFallDistance() > this.jump.getFallDistance()) {
            this.player.setFallDistance(0);
            this.tpLastCheckpoint();
        }
    }

    /**
     * Notify the player has received damages. Used by the {@link fr.ycraft.jump.listeners.GameListener GameListener}
     * @see fr.ycraft.jump.listeners.GameListener
     * @param event original event
     */
    public void onDamage(EntityDamageEvent event) {
        if (EntityDamageEvent.DamageCause.VOID == event.getCause()) this.tpLastCheckpoint();
        event.setCancelled(true);
    }

    /**
     * Notify the player has been teleported. Used by the {@link fr.ycraft.jump.listeners.GameListener GameListener}
     * @see fr.ycraft.jump.listeners.GameListener
     * @param event original event
     */
    public void onTeleport(PlayerTeleportEvent event) {
        if (!this.checkpoint.equals(event.getTo()) && !this.spawnLocation.equals(event.getTo())) {
            this.close();
            Text.LEFT_JUMP_ERROR.send(event.getPlayer(), Text.NO_TELEPORT);
        }
    }

    /**
     * Update the bossbar with new validated checkpoint count
     */
    private void updateBossbar() {
        if (this.bossBarEnabled) return;

        this.bossBar.setProgress((float) this.validated.size() / this.jump.getCheckpoints().size());
        this.bossBar.setTitle(Text.GAME_BOSSBAR.get(
                this.jump.getName(),
                this.validated.size(),
                this.jump.getCheckpoints().size())
        );
    }

    /**
     * Reset the timer to the given duration
     * @param current the duration to set the timer
     */
    public void reset(long current) {
        this.start = current;
        this.validated.clear();
        this.checkpoint = this.startLocation;
        this.updateBossbar();
        this.updateCheckpointsScoreboard(0);
        Text.GAME_CHRONO_RESET.send(this.player);
        this.resetTitle.send(
                this.player,
                Text.GAME_RESET_TITLE.get(),
                Text.GAME_RESET_SUBTITLE.get()
        );
    }

    /**
     * Validate a new checkpoint.
     *
     * Run checkpoint logic, notify ATH and set the given location as the last validated checkpoint
     * @param location the location of the validated checkpoint
     */
    public void validateCheckpoint(Location location) {
        this.checkpoint = location.clone();
        this.validated.add(location);
        this.updateBossbar();
        this.updateCheckpointsScoreboard(this.validated.size());
        Text.GAME_CHECKPOINT.send(this.player);
        this.checkpointTitle.send(
                this.player,
                Text.GAME_CHECKPOINT_TITLE.get(this.validated.size(), this.jump.getCheckpointCount()),
                Text.GAME_CHECKPOINT_SUBTITLE.get(this.validated.size(), this.jump.getCheckpointCount())
        );
    }

    public void tpLastCheckpoint() {
        if (this.checkpoint.equals(this.startLocation)) {
            this.player.teleport(this.spawnLocation);
        } else {
            this.player.teleport(this.checkpoint);
        }

        Text.BACK_TO_CHECKPOINT.send(this.player);
    }

    private void end() {
        this.ended = true;
        TimeScore score = new TimeScore(System.currentTimeMillis() - this.start);
        Text.GAME_END.send(this.player, this.jump.getName(), score.getMinutes(), score.getSeconds(), score.getMillis());
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> this.saveScore(score));

        this.endTitle.send(
                this.player,
                Text.GAME_END_TITLE.get(),
                score.getText(Text.GAME_END_SUBTITLE)
        );

        this.close();
    }

    private void saveScore(TimeScore score) {
        this.jump.registerScore(this.player, score.getDuration(), this.config.get(Key.MAX_SCORES_PER_JUMP));
        this.jumpPlayer.put(this.jump, score);
        this.storage.storePlayer(this.jumpPlayer);
    }

    public void close() {
        this.gameManager.remove(this.player, this);
        this.stop();
    }

    public void stop() {
        if (!this.ended) Text.LEFT_JUMP.send(this.player);
        this.player.setCollidable(this.wasCollidable);
        this.bossBar.removeAll();
        this.player.setScoreboard(this.originalScoreboard);
        this.objective.unregister();
        this.bukkitTask.cancel();

//        this.bossBar = null;
    }
}
