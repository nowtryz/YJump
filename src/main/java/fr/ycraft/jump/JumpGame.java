package fr.ycraft.jump;

import com.google.inject.assistedinject.Assisted;
import fr.ycraft.jump.commands.Perm;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.JumpPlayer;
import fr.ycraft.jump.entity.TimeScore;
import fr.ycraft.jump.manager.GameManager;
import fr.ycraft.jump.manager.PlayerManager;
import fr.ycraft.jump.storage.Storage;
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

    @NotNull
    private final Location startLocation;
    private final List<Location> validated = new LinkedList<>();
    private final Jump jump;
    private final Config config;
    private final JumpPlugin plugin;
    private final JumpPlayer jumpPlayer;
    private final GameManager gameManager;
    private final Storage storage;
    private final Player player;
    private final boolean canFly;
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
        assert jump.getStart().isPresent();
        assert jumpPlayer.isPresent();

        this.jumpPlayer = jumpPlayer.get();
        this.gameManager = gameManager;
        this.storage = storage;
        this.config = config;
        this.resetTime = config.get(Key.RESET_TIME);
        this.plugin = plugin;
        this.jump = jump;
        this.player = player;
        this.startLocation = jump.getStart().get();
        this.start = System.currentTimeMillis();
        this.resetTime = config.get(Key.RESET_TIME);
        this.canFly = Perm.FLY.isHeldBy(player);
        this.checkpoint = this.startLocation;
    }

    public void init() {
        assert Bukkit.isPrimaryThread();
        Text.ENTER_GAME.send(player, jump.getName());

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
        this.player.setScoreboard(scoreboard);

        // Init bossbar
        this.bossBar = Bukkit.createBossBar(
                Text.GAME_BOSSBAR.get(jump.getName(), 0, jump.getCheckpoints().size()),
                plugin.getConfigProvider().get(Key.BOSS_BAR_COLOR),
                BarStyle.SOLID
        );
        this.bossBar.setProgress(0);
        this.bossBar.addPlayer(player);

        // setup timer
        this.bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateTimer, 1, 1);
    }

    public void updateTimer() {
        TimeScore score = new TimeScore(System.currentTimeMillis() - this.start);

        this.scoreboard.resetScores(this.timer.getEntry());
        this.timer = this.objective.getScore(score.getText(Text.SCOREBOARD_TIMER_VALUE));
        this.timer.setScore(TIMER_VALUE_POS);
    }

    public void updateCheckpointsScoreboard(int count) {
        this.scoreboard.resetScores(this.checkpoints.getEntry());
        this.checkpoints = this.objective.getScore(Text.SCOREBOARD_CHECKPOINT_VALUE.get(count, this.jump.getCheckpoints().size()));
        this.checkpoints.setScore(CHECKPOINT_VALUE_POS);
    }

    @NotNull
    public Jump getJump() {
        return jump;
    }

    public void onCommand(PlayerCommandPreprocessEvent event) {
        String[] command = event.getMessage().substring(1).split(" ");
        if (command.length == 0) return;
        if (this.config.get(Key.ALLOWED_COMMANDS).stream().anyMatch(command[0]::equals)) return;

        this.close();
        Text.LEFT_JUMP_ERROR.send(event.getPlayer(), Text.NO_COMMANDS.get());
    }

    public void onFly(PlayerToggleFlightEvent event) {
        if (!this.canFly) {
            event.setCancelled(true);
            Text.NO_FLY.send(event.getPlayer());
        }
    }

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
        if (this.jump.getEnd().map(l -> LocationUtil.isBlockLocationEqual(l, loc)).orElse(false)) {
            if (this.validated.size() == this.jump.getCheckpoints().size()) this.end();
            else Text.GAME_MISSING_CHECKPOINT.send(this.player);
        }
        // chrono reset
        else if (LocationUtil.isBlockLocationEqual(this.startLocation, loc)) {
            long current = System.currentTimeMillis();
            if (current - this.start < this.resetTime) return;
            this.reset(current);
        } else if (this.validated.stream().noneMatch(l -> LocationUtil.isBlockLocationEqual(l, loc))){
            this.jump.getCheckpoints().stream()
                    .filter(l -> LocationUtil.isBlockLocationEqual(l, loc))
                    .findFirst().ifPresent(this::validateCheckpoint);
        }
    }

    public void onMove() {
        if (this.player.getFallDistance() > this.config.get(Key.MAX_FALL_DISTANCE)) {
            this.player.setFallDistance(0);
            this.tpLastCheckpoint();
        }
    }

    public void onDamage(EntityDamageEvent event) {
        if (EntityDamageEvent.DamageCause.VOID == event.getCause()) this.tpLastCheckpoint();
        event.setCancelled(true);
    }

    private void updateBossbar() {
        this.bossBar.setProgress((float) this.validated.size() / this.jump.getCheckpoints().size());
        this.bossBar.setTitle(Text.GAME_BOSSBAR.get(
                this.jump.getName(),
                this.validated.size(),
                this.jump.getCheckpoints().size())
        );
    }

    public void reset(long current) {
        this.start = current;
        this.validated.clear();
        this.checkpoint = this.startLocation;
        this.updateBossbar();
        this.updateCheckpointsScoreboard(0);
        Text.CHRONO_RESET.send(this.player);
    }

    public void validateCheckpoint(Location location) {
        this.checkpoint = location.clone();
        this.validated.add(location);
        this.updateBossbar();
        this.updateCheckpointsScoreboard(this.validated.size());
        Text.CHECKPOINT_VALIDATED.send(this.player);
    }

    public void tpLastCheckpoint() {
        if (this.checkpoint.equals(this.startLocation) && this.jump.getSpawn().isPresent()) {
            this.player.teleport(this.jump.getSpawn().get());
        } else {
            this.player.teleport(this.checkpoint);
        }

        Text.BACK_TO_CHECKPOINT.send(this.player);
    }

    private void end() {
        this.ended = true;
        TimeScore score = new TimeScore(System.currentTimeMillis() - this.start);
        Text.JUMP_END.send(this.player, this.jump.getName(), score.getMinutes(), score.getSeconds(), score.getMillis());
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> this.saveScore(score));

        this.player.sendTitle(
                Text.JUMP_END_TITLE.get(),
                score.getText(Text.JUMP_END_SUBTITLE),
                10, 60, 10);

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
