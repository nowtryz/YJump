package fr.ycraft.jump;

import fr.ycraft.jump.commands.Perm;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.TimeScore;
import fr.ycraft.jump.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class JumpGame  implements Listener {
    private static final int TIMER_HEADER_POS = 5;
    private static final int TIMER_VALUE_POS = 4;
    private static final int CHECKPOINT_HEADER_POS = 2;
    private static final int CHECKPOINT_VALUE_POS = 1;

    @NotNull private final Location startLocation;
    private final List<Location> validated = new LinkedList<>();
    private final JumpPlugin plugin;
    private final Jump jump;
    private final Player player;
    private final long resetTime;
    private final boolean canFly;
    private final BossBar bossBar;
    private final Scoreboard originalScoreboard;
    private final Objective objective;
    private final Scoreboard scoreboard;
    private final BukkitTask bukkitTask;
    private Score timer, checkpoints;
    private boolean ended = false;
    private long start;
    private Location checkpoint;

    public JumpGame(@NotNull JumpPlugin plugin, @NotNull Jump jump, @NotNull Player player) {
        assert jump.getStart().isPresent();

        this.plugin = plugin;
        this.jump = jump;
        this.player = player;
        this.startLocation = jump.getStart().get();
        this.start = System.currentTimeMillis();
        this.resetTime = this.plugin.getConfigProvider().getResetTime();
        this.canFly = Perm.FLY.isHeldBy(player);
        this.checkpoint = this.startLocation;

        if (!this.canFly) player.setFlying(false);
        if (plugin.getConfigProvider().doesResetEnchants()) player
                .getActivePotionEffects()
                .stream()
                .map(PotionEffect::getType)
                .forEach(player::removePotionEffect);

        Text.ENTER_GAME.send(player, jump.getName());

        this.bossBar = Bukkit.createBossBar(
                Text.GAME_BOSSBAR.get(jump.getName(), 0, jump.getCheckpoints().size()),
                plugin.getConfigProvider().getBossbarColor(),
                BarStyle.SOLID
        );
        this.bossBar.setProgress(0);
        this.bossBar.addPlayer(player);

        this.originalScoreboard = player.getScoreboard();
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective(plugin.getName() + player.getName(), "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective.setDisplayName(Text.SCOREBOARD_DISPLAYNAME.get(this.jump.getName()));
        this.objective.getScore(Text.SCOREBOARD_TIMER_HEADER.get()).setScore(TIMER_HEADER_POS);
        this.objective.getScore(Text.SCOREBOARD_CHECKPOINT_HEADER.get()).setScore(CHECKPOINT_HEADER_POS);
        this.objective.getScore(" ").setScore(3);

        this.timer = this.objective.getScore(Text.SCOREBOARD_TIMER_VALUE.get(0, 0, 0));
        this.timer.setScore(TIMER_VALUE_POS);
        this.checkpoints = this.objective.getScore(Text.SCOREBOARD_CHECKPOINT_VALUE.get(0, this.jump.getCheckpoints().size()));
        this.checkpoints.setScore(CHECKPOINT_VALUE_POS);

        this.player.setScoreboard(scoreboard);
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

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (!event.getPlayer().equals(this.player)) return;
        this.close();
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getPlayer().equals(this.player)) return;

        String[] command = event.getMessage().substring(1).split(" ");
        if (command.length == 0) return;
        if (this.plugin.getConfigProvider().getAllowedCommands().stream().anyMatch(command[0]::equals)) return;

        this.close();
        Text.LEFT_JUMP_ERROR.send(event.getPlayer(), Text.NO_COMMANDS.get());
    }

    @EventHandler
    public void onFly(PlayerToggleFlightEvent event) {
        if (!event.getPlayer().equals(this.player) || this.canFly) return;
        this.close();
        Text.LEFT_JUMP_ERROR.send(event.getPlayer(), Text.NO_FLY.get());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!this.player.equals(event.getPlayer())
            ||!event.getAction().equals(Action.PHYSICAL)
            || event.getClickedBlock() == null
            || !Jump.ALLOWED_MATERIALS.contains(event.getClickedBlock().getType())
        ) return;

        Location loc = event.getClickedBlock().getLocation();

        // jump end
        if (this.jump.getEnd().map(l -> LocationUtil.isBlockLocationEqual(l, loc)).orElse(false)) {
            this.end();
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

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!this.player.equals(event.getPlayer())) return;
        if (!(this.plugin.getConfigProvider().getMaxFallDistance() > 0)) return;
        if (this.player.getFallDistance() > this.plugin.getConfigProvider().getMaxFallDistance()) {
            this.player.setFallDistance(0);
            this.tpLastCheckpoint();
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!this.player.equals(event.getEntity())) return;
        if (EntityDamageEvent.DamageCause.VOID.equals(event.getCause())) this.tpLastCheckpoint();
        event.setCancelled(true);
        this.tpLastCheckpoint();
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
        this.jump.registerScore(this.player, score.getDuration(), this.plugin.getConfigProvider().getMaxScoresPerJump());
        this.plugin.getPlayerManager().addNewPlayerScore(this.player, this.jump, score.getDuration());
    }

    public void close() {
        this.plugin.getGameManager().remove(this.player, this);
        this.stop();
    }

    public void stop() {
        if (!this.ended) Text.LEFT_JUMP.send(this.player);
        this.bossBar.removeAll();
        this.player.setScoreboard(this.originalScoreboard);
        this.objective.unregister();
        this.bukkitTask.cancel();
//        this.bossBar = null;
        HandlerList.unregisterAll(this);
    }
}
