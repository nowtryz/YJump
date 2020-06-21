package fr.ycraft.jump;

import com.google.common.base.Charsets;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * This enumeration represents the messages that need to be translated
 */
public enum Text {
    BACK("back", "» Back"),
    BACK_TO_CHECKPOINT("game.checkpoint_tp", "Teleported to last checkpoint"),
    BEST_SCORES_BOOK_LINE("inventory.player.line", "%d) %02d'%02d''%03d"),
    BEST_SCORES_HEADER("inventory.player.header", "Best %2$d scores for %1$s"),
    CHECKPOINT_ADDED("cmd.addcheckpoint.performed", "Checkpoint added for %s"),
    CHECKPOINT_DELETED("editor.delete_checkpoint", "Checkpoint deleted"),
    CHECKPOINT_VALIDATED("game.checkpoint", "Checkpoint validated"),
    CHRONO_RESET("game.reset", "The chrono has been reset"),
    CLICK("click", "Click to execute"),
    CREATE_USAGE("cmd.create.usage", "/jump create <name>"),
    DELETED("deleted", "%s deleted"),
    DELETE_USAGE("cmd.delete.usage", "/jump delete <name>"),
    DESC_ADD_CHECKPOINT("cmd.addcheckpoint.desc", "Adds a checkpoint to the jump currentmy edited"),
    DESC_CHECKPOINT("cmd.checkpoint.desc", "Teleport to last checkpoint"),
    DESC_CREATE("cmd.create.desc", "Create a new jump"),
    DESC_DELETE("cmd.delete.desc", "Deletes the specified jump"),
    DESC_EDIT("cmd.edit.desc", "Enter edit mode for the specified jump and teleport to jump spawn"),
    DESC_HELP("cmd.help.desc", "Shows the description of all commands"),
    DESC_JUMP("cmd.jump.desc", "Default jump command"),
    DESC_JUMPS("cmd.jumps.desc", "Show a gui with the list of available jump"),
    DESC_LEAVE("cmd.leave.desc", "Leave the game while on a jump"),
    DESC_LIST("cmd.list.desc", "Lists all available jumps"),
    DESC_RELOAD("cmd.reload.desc", "Reload jump configurations and files"),
    DESC_SAVE("cmd.save.desc", "Saves jump and stops the edit mode"),
    DESC_SET_END("cmd.setend.desc", "Sets the end location of the currently edited jump"),
    DESC_SET_SPAWN("cmd.setspawn.desc", "Sets the spawn of the jump currently edited jump"),
    DESC_SET_START("cmd.setstart.desc", "Sets the start block of the currently edited jump"),
    EDITOR_NO_GAME("editor.cannot_start_game", "You cannot start a jump while in editor"),
    EDITOR_ONLY_ACTION("only_editor_action", "You must be in an editor to perform this action"),
    EDITOR_ONLY_COMMAND("only_editor_command", "You must be in an editor to perform this command"),
    EDITOR_TITLE("editor.title", "You're currently editing %s"),
    EDIT_USAGE("cmd.edit.usage", "/jump edit <name>"),
    END_UPDATED("cmd.setend.performed", "End position updated for %d"),
    ENTER_EDITOR_CHECKPOINT_PREFIX("editor.enter.checkpoint.prefix", "To add a checkpoint do "),
    ENTER_EDITOR_CHECKPOINT_SUFFIX("editor.enter.checkpoint.suffix", ". to remove one, just break it"),
    ENTER_EDITOR_INFO("editor.enter.info", "You entered the editor for %s"),
    ENTER_EDITOR_INFO_BLOCKS("editor.enter.blocks", "To edit the start and the end of the jump, do ") /* usage */,
    ENTER_EDITOR_INFO_BLOCKS_BTW("editor.enter.blocks_btw", " or "),
    ENTER_EDITOR_INFO_LEAVE("editor.enter.leave", "To quit, do ") /* usage */,
    ENTER_EDITOR_INFO_SPAWN("editor.enter.spawn", "To set the spawn, do ") /* usage */,
    ENTER_GAME("game.enter", "You've stated the jump %s"),
    GAME_BOSSBAR("game.bossbar", "Jump %s > %d/%d"),
    HEADER_EDITOR("editor.header", "§e§m=  §r§e» §6§lJump Editor §r§e«§m  ="),
    HELP_COMMAND("help.command", "%s\n  %s"),
    HELP_HEADER("help.header", "Jump help:"),
    JUMP_ALREADY_EXISTS("already_exists", "This jump already exist"),
    JUMP_END("game.end.message", "You finished the jump %s in %d՚%d՚՚%ds"),
    JUMP_END_SUBTITLE("game.end.subtitle", "ended in %d՚%d՚՚%d"),
    JUMP_END_TITLE("game.end.title", "Jump end"),
    JUMP_INVENTORY("inventory.jump.title", "%s"),
    JUMP_INVENTORY_TP("inventory.jump.tp", "Teleport to jump"),
    JUMP_INVENTORY_TOP("inventory.jump.top", "See top %d"),
    JUMP_INVENTORY_SELF("inventory.jump.self", "See my best scores"),
    JUMP_LIST("list", "Jumps: %s"),
    JUMP_LIST_INVENTORY("inventory.list.title", "Jumps list"),
    JUMP_LIST_HEADER("inventory.list.header", "Jumps list"),
    JUMP_LIST_DONE_LORE("inventory.list.lore.done", "Distance: %1$.2f\nBest score: %3$02d'%4$02d''%5$03d\nYou've done this jump"),
    JUMP_LIST_NEVER_DONE_LORE("inventory.list.lore.never_done", "Distance: %1$.2f\n\nYou've never played this jump"),
    JUMP_NOT_EXISTS("not_exists", "This jump does not exist"),
    LEAVE_USAGE("/jump leave"),
    LEFT_JUMP("game.left_command", "You left the jump"),
    LEFT_JUMP_ERROR("game.left", "You left the jump: %s"),
    LIST_USAGE("/jump list"),
    NO_COMMANDS("game.no_command", "no commands"),
    NO_FLY("game.no_flight", "no flight"),
    NO_PERM("noperm", "§cYou don't have de permission to execute this command"),
    NO_SPAWN("editor.nospawn", "This jump has no spawn location yet"),
    ONLY_GAME_COMMAND("only_game_command", "You must be on a jump to performe this command"),
    ONLY_PLAYER_COMMAND("only_player_command", "Only players can execute tis command"),
    QUIT_EDITOR("quit", "you left the editor"),
    RELOADED("reloaded", "Plugin reloaded"),
    SAVE_USAGE("/jump save"),
    SCOREBOARD_CHECKPOINT_HEADER("game.scoreboard.checkpoint.header", "Checkpoint:"),
    SCOREBOARD_CHECKPOINT_VALUE("game.scoreboard.checkpoint.value", "  %d / %d"),
    SCOREBOARD_DISPLAYNAME("game.scoreboard.displayname", "Jump %s"),
    SCOREBOARD_TIMER_HEADER("game.scoreboard.timer.header", "Time:"),
    SCOREBOARD_TIMER_VALUE("game.scoreboard.timer.value", "  %02d'%02d''%02d"),
    SPAWN_UPDATED("cmd.setspawn.performed", "Spawn updated for %s"),
    START_UPDATED("cmd.setstart.performed", "Start position updated for %s"),
    TOP_INVENTORY_TITLE("inventory.top.name", "TOP 10"),
    TOP_SCORE_LORE("inventory.top.score.lore", "%1$s\n%3$02d'%4$02d''%5$03d"),
    TOP_SCORE_TITLE("inventory.top.score.name", "TOP %2$d"),
    UNKNOWN_COMMAND("unknown_command", ChatColor.RED + "Unknown Command"),
    USAGE("usage", ChatColor.RED + "Usage: %s");


    /**
     * YAML file containing all translation
     */
    private static FileConfiguration lang;
    private static final String DEFAULT_LANG = "fr-FR"; // may be change in config later

    /**
     * Initialize the translator base on the default language of the plugin
     * @param plugin the Bukkit plugin to get resources from
     */
    static void init(JumpPlugin plugin) {
        try {
            String fileName = DEFAULT_LANG + ".yml";
            InputStream resource = plugin.getResource(fileName);
            File file = new File(plugin.getDataFolder(), fileName);
            Validate.notNull(resource, String.format("Unable to find language file for '%s'", DEFAULT_LANG));
            Text.lang = YamlConfiguration.loadConfiguration(file);
            Text.lang.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(resource, Charsets.UTF_8)));
            resource.close();
        } catch (Exception exception) {
            throw new RuntimeException("Unable to correctly load the language file", exception);
        }
    }

    /**
     * The key to access to the message in the translations file
     */
    private final String key;
    /**
     * The default message to use if no translation is available in the file
     */
    private final String defaultMessage;

    /**
     * Create a new text message with information to retrieve the translation from the file
     * @param key
     * @param defaultMessage
     */
    Text(String key, String defaultMessage) {
        this.key = key;
        this.defaultMessage = defaultMessage;
    }

    /**
     * Creates a new text message that doesn't need any translation
     * @param defaultMessage the message to use
     */
    Text(String defaultMessage) {
        this(null, defaultMessage);
    }

    /**
     * Translate this message and return the translated version
     * @return the translated message
     */
    public String get() {
        if (this.key == null) return this.defaultMessage;
        if (Text.lang.isList(this.key)) {
            return Optional.ofNullable(Text.lang.getStringList(this.key))
                    .map(strings -> String.join("\n", strings))
                    .orElse(this.defaultMessage);
        }
        return Optional.ofNullable(Text.lang.getString(this.key)).orElse(this.defaultMessage);
    }

    public String get(Object... args) {
        return String.format(this.get(), args);
    }

    /**
     * Send the message to a command sender, usually a player
     * @param p the CommandSender that will receive the message
     */
    public void send(CommandSender p) {
        p.sendMessage(this.get());
    }

    public void send(CommandSender p, Object... args) {
        p.sendMessage(this.get(args));
    }

    @Override
    public String toString() {
        return this.get();
    }
}
