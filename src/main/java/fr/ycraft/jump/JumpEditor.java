package fr.ycraft.jump;

import com.google.inject.assistedinject.Assisted;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.manager.JumpManager;
import fr.ycraft.jump.storage.Storage;
import lombok.experimental.FieldDefaults;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@FieldDefaults(makeFinal = true)
public class JumpEditor {
    JumpManager jumpManager;
    JumpPlugin plugin;
    Storage storage;
    Config config;
    Jump jump;

    Set<Player> players = new LinkedHashSet<>();
    Map<Player, GameMode> gameModes = new ConcurrentHashMap<>();
    BukkitTask bukkitTask;

    @Inject
    public JumpEditor(JumpPlugin plugin, JumpManager jumpManager, Config config, Storage storage, @Assisted Jump jump) {
        this.plugin = plugin;
        this.jumpManager = jumpManager;
        this.config = config;
        this.storage = storage;
        this.jump = jump;
        this.bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateTitles, 50, 50);
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public Jump getJump() {
        return jump;
    }

    public void updateTitles() {
        TextComponent title = new TextComponent(Text.EDITOR_TITLE.get(this.jump.getName()));
        this.players.forEach(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, title));
    }

    public void join(Player player) {
        this.players.add(player);
        Text.HEADER_EDITOR.send(player);
        Text.ENTER_EDITOR_INFO.send(player, this.jump.getName());
        this.updateTitles();

        if (this.config.get(Key.CREATIVE_EDITOR)) Bukkit.getScheduler().runTask(this.plugin, () ->{
            this.gameModes.put(player, player.getGameMode());
            player.setGameMode(GameMode.CREATIVE);
        });

        player.spigot().sendMessage(
            new ComponentBuilder(Text.ENTER_EDITOR_INFO_LEAVE.get())
                .append(new ComponentBuilder(CommandSpec.SAVE.getUsage())
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                        new TextComponent(Text.CLICK.get())
                    }))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, CommandSpec.SAVE.getUsage()))
                    .color(ChatColor.GREEN.asBungee())
                    .italic(true)
                    .create()
                , FormatRetention.NONE)
            .create()
        );

        player.spigot().sendMessage(
            new ComponentBuilder(Text.ENTER_EDITOR_INFO_SPAWN.get())
                .append(new ComponentBuilder(CommandSpec.SET_SPAWN.getUsage())
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                        new TextComponent(Text.CLICK.get())
                    }))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, CommandSpec.SET_SPAWN.getUsage()))
                    .color(ChatColor.GREEN.asBungee())
                    .italic(true)
                    .create()
                , FormatRetention.NONE)
            .create()
        );

        player.spigot().sendMessage(
            new ComponentBuilder(Text.ENTER_EDITOR_INFO_BLOCKS.get())
                .append(new ComponentBuilder(CommandSpec.SET_START.getUsage())
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                            new TextComponent(Text.CLICK.get())
                    }))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, CommandSpec.SET_START.getUsage()))
                    .color(ChatColor.GREEN.asBungee())
                    .italic(true)
                    .create()
                , FormatRetention.NONE)
                .append(Text.ENTER_EDITOR_INFO_BLOCKS_BTW.get(), FormatRetention.NONE)
                .append(new ComponentBuilder(CommandSpec.SET_END.getUsage())
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                            new TextComponent(Text.CLICK.get())
                    }))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, CommandSpec.SET_END.getUsage()))
                    .color(ChatColor.GREEN.asBungee())
                    .italic(true)
                    .create()
                , FormatRetention.NONE)
            .create()
        );

        player.spigot().sendMessage(
                new ComponentBuilder(Text.ENTER_EDITOR_CHECKPOINT_PREFIX.get())
                        .append(new ComponentBuilder(CommandSpec.ADD_CHECKPOINT.getUsage())
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                                        new TextComponent(Text.CLICK.get())
                                }))
                                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, CommandSpec.ADD_CHECKPOINT.getUsage()))
                                .color(ChatColor.GREEN.asBungee())
                                .italic(true)
                                .create()
                        , FormatRetention.NONE)
                        .append(Text.ENTER_EDITOR_CHECKPOINT_SUFFIX.get(), FormatRetention.NONE)
                        .create()
        );

        if (this.jump.getSpawn().isPresent()) {
            player.teleport(this.jump.getSpawn().get());
        } else {
            Text.NO_SPAWN.send(player);
        }
    }

    public void setSpawn(Location location) {
        this.jump.setSpawn(location);
        this.storage.storeJump(this.jump);
        this.players.forEach(player -> Text.SPAWN_UPDATED.send(player, this.jump.getName()));
    }

    public void setStart(Location location) {
        if (this.config.get(Key.DELETE_PLATES)) {
            this.jump.getStart().map(Location::getBlock).ifPresent(block -> block.setType(Material.AIR));
        }

        this.jump.setStart(location);
        this.storage.storeJump(this.jump);

        if (location != null) {
            this.ensureSafeLocation(location);
            location.getBlock().setType(this.config.get(Key.START_MATERIAL));
        }

        this.players.forEach(player -> Text.START_UPDATED.send(player, this.jump.getName()));
    }

    public void ensureSafeLocation(Location location) {
        Block down = location.getBlock().getRelative(BlockFace.DOWN);
        if (!down.getType().isOccluding()) down.setType(Material.GOLD_BLOCK);
    }

    public void setSEnd(Location location) {
        if (this.config.get(Key.DELETE_PLATES)) {
            this.jump.getEnd().map(Location::getBlock).ifPresent(block -> block.setType(Material.AIR));
        }

        this.jump.setEnd(location);
        this.storage.storeJump(this.jump);

        if (location != null) {
            this.ensureSafeLocation(location);
            location.getBlock().setType(this.config.get(Key.END_MATERIAL));
        }

        this.players.forEach(player -> Text.END_UPDATED.send(player, this.jump.getName()));
    }

    public void addCheckpoint(@NotNull Location location) {
        this.jump.addCheckpoint(location);
        this.storage.storeJump(this.jump);
        this.ensureSafeLocation(location);
        location.getBlock().setType(this.config.get(Key.CHECKPOINT_MATERIAL));
        this.players.forEach(player -> Text.CHECKPOINT_ADDED.send(player, this.jump.getName()));
    }

    public void deleteCheckpoint(@NotNull Location location) {
        this.jump.removeCheckpoint(location);
        this.players.forEach(Text.CHECKPOINT_DELETED::send);
        this.storage.storeJump(this.jump);

        if (this.config.get(Key.DELETE_PLATES)) {
            location.getBlock().setType(Material.AIR);
        }
    }

    public void leave(Player player) {
        this.players.remove(player);
        Text.QUIT_EDITOR.send(player);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(""));

        if (this.config.get(Key.CREATIVE_EDITOR)) Bukkit.getScheduler().runTask(this.plugin, () -> {
            Optional.ofNullable(this.gameModes.get(player)).ifPresent(player::setGameMode);
            this.gameModes.remove(player);
        });

        if (this.players.isEmpty()) this.close();
    }

    public void close() {
        this.players.forEach(this::leave);
        this.bukkitTask.cancel();
        this.storage.storeJump(this.jump);
        this.jumpManager.updateJumpList();
    }
}
