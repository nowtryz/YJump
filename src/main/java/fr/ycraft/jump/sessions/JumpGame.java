package fr.ycraft.jump.sessions;

import com.google.inject.assistedinject.Assisted;
import fr.mrmicky.fastboard.FastBoard;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.commands.enums.Perm;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.configuration.TitleSettings;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.JumpPlayer;
import fr.ycraft.jump.entity.Position;
import fr.ycraft.jump.entity.TimeScore;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.manager.GameManager;
import fr.ycraft.jump.manager.PlayerManager;
import fr.ycraft.jump.storage.Storage;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class JumpGame {
    private final @Getter(onMethod_={@NotNull}) Jump jump;
    private final @NotNull Location spawnLocation;
    private final @NotNull Position startLocation;
    private final @NotNull Position endLocation;
    private final List<Position> validated = new LinkedList<>();
    private final Config config;
    private final FastBoard board;
    private final JumpPlugin plugin;
    private final JumpPlayer jumpPlayer;
    private final GameManager gameManager;
    private final Storage storage;
    private final Player player;
    private final Scoreboard originalScoreboard;
    private final TitleSettings startTitle;
    private final TitleSettings resetTitle;
    private final TitleSettings checkpointTitle;
    private final TitleSettings endTitle;
    private final boolean fallPrevention;
    private final boolean intelligentFall;
    private final boolean canFly;
    private final boolean bossBarEnabled;
    private final boolean sidebarEnabled;
    private final int checkpointCount;
    private final World world;
    private BossBar bossBar;
    private BukkitTask bukkitTask;
    private int fallDistance;
    private long resetTime;
    private boolean ended = false;
    private boolean wasCollidable = true;
    private long start;
    private Position nextCheckpoint;
    private Position checkpoint;

    @Inject
    JumpGame(Config config,
             PlayerManager playerManager,
             GameManager gameManager,
             Storage storage,
             JumpPlugin plugin,
             @Assisted Jump jump,
             @Assisted Player player) {

        Optional<JumpPlayer> jumpPlayer = playerManager.getPlayer(player);
        Optional<Position> start = jump.getStartPos();
        Optional<Location> spawn = jump.getSpawn();
        Optional<Position> end = jump.getEndPos();
        assert start.isPresent();
        assert spawn.isPresent();
        assert end.isPresent();
        assert jumpPlayer.isPresent();

        this.world = jump.getWorld();
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
        this.checkpointCount = jump.getCheckpointCount();
        this.player = player;
        this.startLocation = start.get();
        this.spawnLocation = spawn.get();
        this.endLocation = end.get();
        this.start = System.currentTimeMillis();
        this.resetTime = config.get(Key.RESET_TIME);
        this.canFly = player.hasPermission(Perm.FLY);
        this.fallPrevention = jump.getFallDistance() > 0;
        this.intelligentFall = config.get(Key.INTELLIGENT_FALL_DISTANCE);
        this.fallDistance = jump.getFallDistance();
        this.checkpoint = this.startLocation;
        this.nextCheckpoint = jump.getCheckpointCount() > 0 ? jump.getCheckpointsPositions().get(0) : this.endLocation;
        this.originalScoreboard = player.getScoreboard();
        this.board = this.sidebarEnabled ? new FastBoard(player) : null; // It is strange but we can send player packets async
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
        if (this.sidebarEnabled) {
            this.board.updateTitle(Text.SCOREBOARD_DISPLAY_NAME.get(this.jump.getName()));
            this.updateBoard();
            this.bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateBoard, 1, 1);
        }

        // Init boss bar
        if (this.bossBarEnabled) {
            this.bossBar = Bukkit.createBossBar(
                    Text.GAME_BOSSBAR.get(jump.getName(), 0, jump.getCheckpoints().size()),
                    plugin.getConfigProvider().get(Key.BOSS_BAR_COLOR),
                    BarStyle.SOLID
            );
            this.bossBar.setProgress(0);
            this.bossBar.addPlayer(player);
        }

        this.computeFallDistance();
    }

    /**
     * Method called every tick by bukkit to update the sidebar
     */
    public void updateBoard() {
        if (this.sidebarEnabled) this.board.updateLines(Text.SCOREBOARD_LINES.get(
                System.currentTimeMillis() - this.start,
                this.validated.size(),
                this.jump.getCheckpointCount()
        ).split(StringUtils.LF));
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
        if (this.endLocation.isBlock(loc)) {
            if (this.validated.size() == this.jump.getCheckpoints().size()) this.end();
            else Text.GAME_MISSING_CHECKPOINT.send(this.player);
        }
        // chrono reset
        else if (this.startLocation.isBlock(loc)) {
            long current = System.currentTimeMillis();
            if (current - this.start < this.resetTime) return;
            this.reset(current);
        }
        // Validate checkpoint
        else if (this.nextCheckpoint.isBlock(loc)){
            this.validateCheckpoint(this.nextCheckpoint);
        }
    }

    /**
     * Notify the player moved to identity its fall distance.
     * Used by the {@link fr.ycraft.jump.listeners.GameListener GameListener}
     * @see fr.ycraft.jump.listeners.GameListener
     */
    public void onMove() {
        if (this.fallPrevention && this.player.getFallDistance() > this.fallDistance) {
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
        if (!this.checkpoint.isBlock(event.getTo()) && !this.spawnLocation.equals(event.getTo())) {
            this.close();
            Text.LEFT_JUMP_ERROR.send(event.getPlayer(), Text.NO_TELEPORT);
        }
    }

    /**
     * Update the boss bar with new validated checkpoint count
     */
    private void updateBossBar() {
        if (!this.bossBarEnabled) return;

        // prevent division by zero
        if (this.checkpointCount == 0 ) this.bossBar.setProgress(1);
        else this.bossBar.setProgress((float) this.validated.size() / this.jump.getCheckpoints().size());

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
        this.nextCheckpoint = jump.getCheckpointCount() > 0 ? jump.getCheckpointsPositions().get(0) : this.endLocation;
        this.updateBossBar();
        this.updateBoard();
        this.computeFallDistance();
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
     * @param pos the position of the validated checkpoint
     */
    public void validateCheckpoint(Position pos) {
        this.checkpoint = pos.clone();
        this.validated.add(pos);
        this.nextCheckpoint = jump.getCheckpointCount() > this.validated.size() ? jump.getCheckpointsPositions().get(this.validated.size()) : this.endLocation;
        this.updateBossBar();
        this.updateBoard();
        Text.GAME_CHECKPOINT.send(this.player);
        this.checkpointTitle.send(
                this.player,
                Text.GAME_CHECKPOINT_TITLE.get(this.validated.size(), this.jump.getCheckpointCount()),
                Text.GAME_CHECKPOINT_SUBTITLE.get(this.validated.size(), this.jump.getCheckpointCount())
        );

        // Run fall distance update 1 tick later, so the player is not teleport if they were falling
        Bukkit.getScheduler().runTaskLater(this.plugin, this::computeFallDistance, 1L);
    }

    private void computeFallDistance() {
        if (!this.fallPrevention || !this.intelligentFall) return;

        int checkpointElevation = this.checkpoint.getY() - this.nextCheckpoint.getY();
        // if the elevation is superior to the fall distance, taking the jump into account
        if (checkpointElevation > this.jump.getFallDistance() + 1) {
            this.fallDistance = checkpointElevation + jump.getFallDistance();
        } else {
            this.fallDistance = jump.getFallDistance();
        }
    }

    public void tpLastCheckpoint() {
        if (this.checkpoint.equals(this.startLocation)) {
            this.player.teleport(this.spawnLocation);
        } else {
            this.player.teleport(this.checkpoint.toLocation(this.world));
        }

        Text.BACK_TO_CHECKPOINT.send(this.player);
    }

    private void end() {
        this.ended = true;
        TimeScore score = new TimeScore(System.currentTimeMillis() - this.start);
        Text.GAME_END.send(this.player, this.jump.getName(), score.getDuration());
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> this.saveScore(score));

        this.endTitle.send(
                this.player,
                Text.GAME_END_TITLE.get(),
                Text.GAME_END_SUBTITLE.get(score.getDuration())
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
        if (this.sidebarEnabled) this.board.delete();
        if (this.bossBarEnabled) this.bossBar.removeAll();
        this.player.setCollidable(this.wasCollidable);
        this.player.setScoreboard(this.originalScoreboard);
        this.bukkitTask.cancel();

//        this.bossBar = null;
    }
}
