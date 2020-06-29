package fr.ycraft.jump.manager;

import fr.ycraft.jump.JumpEditor;
import fr.ycraft.jump.JumpGame;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.commands.Perm;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GameManager extends AbstractManager implements Listener {
    private final Map<Player, JumpGame> runningGames = new LinkedHashMap<>();
    private Map<Location, Jump> jumpStarts;
    private List<Location> protectedLocations;
    private List<World> protectedWorlds;

    public GameManager(JumpPlugin plugin) {
        super(plugin);
        this.updateJumpList();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void updateJumpList() {
        Map<String, Jump> jumps = this.plugin.getJumpManager().getJumps();
        this.jumpStarts = jumps.values()
                .stream().parallel()
                .filter(jump -> jump.getStart().isPresent())
                .filter(jump -> jump.getEnd().isPresent())
                .collect(Collectors.toMap(jump -> jump.getStart().get(), Function.identity()));

        this.protectedLocations = new ArrayList<>();
        this.protectedLocations.addAll(this.jumpStarts.keySet());
        this.protectedLocations.addAll(jumps.values().stream().parallel()
                .map(Jump::getEnd)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
        this.protectedLocations.addAll(jumps.values().stream().parallel()
                .map(Jump::getCheckpoints)
                .flatMap(List::stream)
                .collect(Collectors.toList())
        );
        this.protectedLocations = this.protectedLocations.stream().parallel()
                .map(LocationUtil::toBlock)
                .collect(Collectors.toList());
        this.protectedWorlds = this.protectedLocations.stream().parallel()
                .map(Location::getWorld)
                .distinct()
                .collect(Collectors.toList());
    }

    public boolean isPlaying(Player player) {
        return this.runningGames.containsKey(player);
    }

    public Optional<JumpGame> getGame(Player player) {
        return Optional.ofNullable(this.runningGames.get(player));
    }

    public void enter(Player player, Jump jump) {
        JumpGame game = new JumpGame(this.plugin, jump, player);
        Bukkit.getPluginManager().registerEvents(game, this.plugin);
        this.runningGames.put(player, game);
    }

    public Collection<JumpGame> getGames() {
        return this.runningGames.values();
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreakInEditor(BlockBreakEvent event) {
        // Avoid interaction in editors
        if (this.plugin.getConfigProvider().isCreativeEditor() &&
                this.plugin.getEditorsManager().getEditor(event.getPlayer())
                        .filter(e -> !Perm.EDITOR_INTERACTIONS.isHeldBy(event.getPlayer()))
                        .isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (
            !this.plugin.getEditorsManager().isInEditor(event.getPlayer()) &&
            !this.plugin.getConfigProvider().isPlatesProtected() ||
            !this.protectedWorlds.contains(event.getBlock().getWorld())
        ) return;

        Location loc = event.getBlock().getLocation();
        Location top = loc.clone();
        top.setY(top.getBlockY() + 1);

        if (this.protectedLocations.contains(loc) || this.protectedLocations.contains(top)) {
            if (Perm.EDIT.isHeldBy(event.getPlayer())) {
                if (!this.plugin.getEditorsManager().isInEditor(event.getPlayer())) {
                    event.setCancelled(true);
                    Text.EDITOR_ONLY_ACTION.send(event.getPlayer());
                } else this.plugin.getEditorsManager()
                    .getEditor(event.getPlayer()).ifPresent(editor -> this.onInteractInEditor(event, editor));
            } else {
                event.setCancelled(true);
            }
        }
    }

    public void onInteractInEditor(@NotNull BlockBreakEvent event, @NotNull JumpEditor editor) {
        Jump jump = editor.getJump();
        Location loc = event.getBlock().getLocation();

        if (jump.getStart().map(l -> LocationUtil.isBlockLocationEqual(l, loc)).orElse(false)) {
            editor.setStart(null);
        } else if (jump.getEnd().map(l -> LocationUtil.isBlockLocationEqual(l, loc)).orElse(false)) {
            editor.setSEnd(null);
        } else if (jump.getCheckpoints().stream()
            .filter(l -> LocationUtil.isBlockLocationEqual(l, loc))
            .peek(editor::deleteCheckpoint)
            .count() == 0) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (
            !event.getAction().equals(Action.PHYSICAL)
            || event.getClickedBlock() == null
            || !Jump.ALLOWED_MATERIALS.contains(event.getClickedBlock().getType())
        ) return;

        Location loc = event.getClickedBlock().getLocation();

        if (!this.isPlaying(event.getPlayer())) {
            for (Location start: this.jumpStarts.keySet()) {
                if (LocationUtil.isBlockLocationEqual(start, loc)) {
                    if (!this.plugin.getEditorsManager().isInEditor(event.getPlayer())) {
                        this.enter(event.getPlayer(), this.jumpStarts.get(start));
                    } else {
                        Text.EDITOR_NO_GAME.send(event.getPlayer());
                     }
                    break;
                }
            }
        }
    }

    public void remove(Player player, JumpGame game) {
        this.runningGames.remove(player, game);
    }

    public void stopAll() {
        this.runningGames.values().forEach(JumpGame::stop);
        this.runningGames.clear();
    }
}
