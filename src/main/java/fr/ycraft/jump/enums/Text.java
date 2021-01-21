package fr.ycraft.jump.enums;

import com.google.common.base.Charsets;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.exceptions.LocaleInitializationException;
import net.nowtryz.mcutils.api.Translation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This enumeration represents the messages that need to be translated
 */
public enum Text implements Translation {
    BACK("back", "» Back"),
    BACK_TO_CHECKPOINT("game.checkpoint_tp", "Teleported to last checkpoint"),
    BEST_SCORES_BOOK_LINE("inventory.player.line", "%d) %02d'%02d''%03d"),
    BEST_SCORES_HEADER("inventory.player.header", "Best %2$d scores for %1$s"),
    CHECKPOINT_ADDED("editor.updated.checkpoint", "Checkpoint added for %s"),
    CHECKPOINT_ALREADY_EXIST("editor.checkpoint_exists", "Checkpoint already exists"),
    CHECKPOINT_DELETED("editor.delete_checkpoint", "Checkpoint deleted"),
    CLICK("click", "Click to execute"),
    COMMAND_ERROR("command_error", "Unable to execute command"),
    CREATE_USAGE("cmd.usage.create", "/jump create <name>"),
    DELETED("deleted", "%s deleted"),
    DELETE_USAGE("cmd.usage.delete", "/jump delete <name>"),
    DESCRIPTION_UPDATED("editor.updated.description", "Description updated"),
    DESC_ADD_CHECKPOINT("cmd.description.addcheckpoint", "Adds a checkpoint to the jump currentmy edited"),
    DESC_CHECKPOINT("cmd.description.checkpoint", "Teleport to last checkpoint"),
    DESC_CREATE("cmd.description.create", "Create a new jump"),
    DESC_DELETE("cmd.description.delete", "Deletes the specified jump"),
    DESC_EDIT("cmd.description.edit", "Enter edit mode for the specified jump and teleport to jump spawn"),
    DESC_HELP("cmd.description.help", "Shows the description of all commands"),
    DESC_INFO("cmd.description.info", "Show information relative to the parameters of a jump"),
    DESC_JUMP("cmd.description.jump", "Default jump command"),
    DESC_JUMPS("cmd.description.jumps", "Show a gui with the list of available jump"),
    DESC_LEAVE("cmd.description.leave", "Leave the game while on a jump"),
    DESC_LIST("cmd.description.list", "Lists all available jumps"),
    DESC_RELOAD("cmd.description.reload", "Reload jump configurations and files"),
    DESC_RENAME("cmd.description.rename", "Rename the jump currently edited"),
    DESC_SAVE("cmd.description.save", "Saves jump and stops the edit mode"),
    DESC_SET_DESCRIPTION("cmd.description.setdesc", "Change the jump description"),
    DESC_SET_END("cmd.description.setend", "Sets the end location of the currently edited jump"),
    DESC_SET_ITEM("cmd.description.setitem", "Sets the item you're holding as the 'logo' of the currently edited jump"),
    DESC_SET_SPAWN("cmd.description.setspawn", "Sets the spawn of the jump currently edited jump"),
    DESC_SET_START("cmd.description.setstart", "Sets the start block of the currently edited jump"),
    DESC_SET_WORLD("cmd.description.setworld", "Change the world in which is registered the specified jump"),
    EDITOR_NO_GAME("editor.cannot_start_game", "You cannot start a jump while in editor"),
    EDITOR_ONLY_ACTION("only_editor_action", "You must be in an editor to perform this action"),
    EDITOR_ONLY_COMMAND("only_editor_command", "You must be in an editor to perform this command"),
    EDITOR_TITLE("editor.title", "You're currently editing %s"),
    EDIT_USAGE("cmd.usage.edit", "/jump edit <name>"),
    EMPTY_SCORE("inventory.top.empty", "Empty slot"),
    END_UPDATED("editor.updated.end", "End position updated for %d"),
    ENTER_EDITOR_CHECKPOINT_PREFIX("editor.enter.checkpoint.prefix", "To add a checkpoint do "),
    ENTER_EDITOR_CHECKPOINT_SUFFIX("editor.enter.checkpoint.suffix", ". to remove one, just break it"),
    ENTER_EDITOR_INFO("editor.enter.info", "You entered the editor for %s"),
    ENTER_EDITOR_INFO_BLOCKS("editor.enter.blocks", "To edit the start and the end of the jump, do ") /* usage */,
    ENTER_EDITOR_INFO_BLOCKS_BTW("editor.enter.blocks_btw", " or "),
    ENTER_EDITOR_INFO_LEAVE("editor.enter.leave", "To quit, do ") /* usage */,
    ENTER_EDITOR_INFO_SPAWN("editor.enter.spawn", "To set the spawn, do ") /* usage */,
    ERROR("error", "An error occurred!"),
    FALL_DISTANCE_INVENTORY_DECREASE("inventory.fall_distance.decrease", "Decrease"),
    FALL_DISTANCE_INVENTORY_DECREASE_10("inventory.fall_distance.decrease10", "Decrease by 10"),
    FALL_DISTANCE_INVENTORY_DISABLED("inventory.fall_distance.disabled", "%dm\nClick to reset to %d"),
    FALL_DISTANCE_INVENTORY_ICON("inventory.fall_distance.icon", "Fall distance"),
    FALL_DISTANCE_INVENTORY_INCREASE("inventory.fall_distance.increase", "Increase"),
    FALL_DISTANCE_INVENTORY_INCREASE_10("inventory.fall_distance.increase10", "Increase by 10"),
    FALL_DISTANCE_INVENTORY_LORE("inventory.fall_distance.lore", "%dm\nClick to reset to %d"),
    FALL_DISTANCE_INVENTORY_TITLE("inventory.fall_distance.title", "Edit fall distance"),
    GAME_ALREADY_EXISTS("already_exists", "This jump already exist"),
    GAME_BOSSBAR("game.bossbar", "Jump %s > %d/%d"),
    GAME_CHECKPOINT("game.checkpoint.message", "Checkpoint validated"),
    GAME_CHECKPOINT_SUBTITLE("game.checkpoint.subtitle", "(%d/%d)"),
    GAME_CHECKPOINT_TITLE("game.checkpoint.title", "Checkpoint"),
    GAME_CHRONO_RESET("game.reset.message", "The chrono has been reset to 0"),
    GAME_END("game.end.message", "You finished the jump %s in %d՚%d՚՚%ds"),
    GAME_END_SUBTITLE("game.end.subtitle", "ended in %d՚%d՚՚%d"),
    GAME_END_TITLE("game.end.title", "Jump end"),
    GAME_ENTER("game.enter.message", "You've stated the jump %s"),
    GAME_ENTER_SUBTITLE("game.enter.subtitle", "Run until the end"),
    GAME_ENTER_TITLE("game.enter.title", "%s"),
    GAME_MISSING_CHECKPOINT("game.missing_checkpoint", "You must validate all checkpoints"),
    GAME_RESET_SUBTITLE("game.reset.subtitle", "You time has been reset "),
    GAME_RESET_TITLE("game.reset.title", "Chrono reset"),
    HEADER_EDITOR("editor.header", "===== Jump Editor ====="),
    HELP_COMMAND("help.command", "%s\n  %s"),
    HELP_HEADER("help.header", "Jump help:"),
    INFO_CHECKPOINT_NAME("inventory.info.name.checkpoint", "Checkpoint"),
    INFO_END_NAME("inventory.info.name.end", "spawn"),
    INFO_FALL_DISABLED("inventory.info.lore.disabled", "Disabled"),
    INFO_FALL_LORE("inventory.info.lore.fall", "%dm"),
    INFO_FALL_NAME("inventory.info.name.fall", "spawn"),
    INFO_ICON_LORE("inventory.info.lore.icon", "%s"),
    INFO_ICON_NAME("inventory.info.name.icon", "icon"),
    INFO_POINT_NOT_SET_LORE("inventory.info.lore.not_set", "Point not set"),
    INFO_POINT_SET_LORE("inventory.info.lore.set", "Position set\nX=%.2f\nY=%.2f\nZ=%.2f"),
    INFO_SPAWN_NAME("inventory.info.name.spawn", "spawn"),
    INFO_START_NAME("inventory.info.name.start", "spawn"),
    INFO_TITLE("inventory.info.title", "Information: %s"),
    INFO_WORLD_LORE("inventory.info.lore.world", "%s"),
    INFO_WORLD_NAME("inventory.info.name.world", "world"),
    INFO_WORLD_NOT_SET("inventory.info.lore.world_not_set", "Not set\nUse /jump setworld %s <world>"),
    ITEM_AIR("editor.set_air_item", "Seriously?"),
    ITEM_UPDATED("editor.updated.item", "Item updated to %s"),
    JUMP_INFO("cmd.success.info", "name: %s\n" +
            "Fall distance: %d\n" +
            "Description: %s\n" +
            "World: %s\n" +
            "Icon: %s\n" +
            "Checkpoints: %d\n" +
            "Spawn: x=%d, y=%d, z=%d\n" +
            "Start: x=%d, y=%d, z=%d\n" +
            "End: x=%d, y=%d, z=%d"),
    JUMP_INVENTORY("inventory.jump.title", "%s"),
    JUMP_INVENTORY_SELF("inventory.jump.self", "See my best scores"),
    JUMP_INVENTORY_SETTINGS("inventory.jump.settings", "Jump settings"),
    JUMP_INVENTORY_TOP("inventory.jump.top", "See top %d"),
    JUMP_INVENTORY_TP("inventory.jump.tp", "Teleport to jump"),
    JUMP_LIST("list", "Jumps: %s"),
    JUMP_LIST_DONE_LORE("inventory.list.lore.done", "%1$s\nDistance: %2$.2f\nBest score: %3$02d'%4$02d''%5$03d\nYou've done this jump"),
    JUMP_LIST_HEADER("inventory.list.header", "Jumps list"),
    JUMP_LIST_INVENTORY("inventory.list.title", "Jumps list"),
    JUMP_LIST_NEVER_DONE_LORE("inventory.list.lore.never_done", "%1$s\nDistance: %2$.2f\n\nYou've never played this jump"),
    JUMP_NOT_EXISTS("not_exists", "This jump does not exist"),
    LEAVE_USAGE("/jump leave"),
    LEFT_JUMP("game.left_command", "You left the jump"),
    LEFT_JUMP_ERROR("game.left", "You left the jump: %s"),
    LIST_USAGE("/jump list"),
    NAME_TOO_LONG("name_too_long", "The name is too long, must be less than 10 characters"),
    NAME_UPDATED("editor.updated.name", "Jump renamed to %s"),
    NEXT_PAGE("inventory.next", "Next page (%d/%d)"),
    NO_COMMANDS("game.no_command", "no commands"),
    NO_FLY("game.no_flight", "no flight allowed"),
    NO_PERM("noperm", "§cYou don't have de permission to execute this command"),
    NO_SPAWN("editor.nospawn", "This jump has no spawn location yet"),
    NO_TELEPORT("game.no_teleport", "no teleportation"),
    ONLY_GAME_COMMAND("only_game_command", "You must be on a jump to performe this command"),
    ONLY_PLAYER_COMMAND("only_player_command", "Only players can execute tis command"),
    PREVIOUS_PAGE("inventory.previous", "Previous page (%d/%d)"),
    QUIT_EDITOR("editor.quit", "you left the editor"),
    RELOADED("reloaded", "Plugin reloaded"),
    RENAME_USAGE("cmd.usage.rename", "/jump rename <new name>"),
    SAVED("saved", "Modifications saved"),
    SCOREBOARD_CHECKPOINT_HEADER("game.scoreboard.checkpoint.header", "Checkpoint:"),
    SCOREBOARD_CHECKPOINT_VALUE("game.scoreboard.checkpoint.value", "  %d / %d"),
    SCOREBOARD_DISPLAY_NAME("game.scoreboard.displayname", "Jump %s"),
    SCOREBOARD_LINES("game.scoreboard.lines", "Time:\n  &9%1$02d'%2$02d''%3$02d&7\nCheckpoint:\n  %4$d / %5$d"),
    SCOREBOARD_TIMER_HEADER("game.scoreboard.timer.header", "Time:"),
    SCOREBOARD_TIMER_VALUE("game.scoreboard.timer.value", "  %02d'%02d''%02d"),
    SET_DESCRIPTION_USAGE("cmd.usage.setdesc", "/jump setdesc <description>"),
    SET_WORLD_USAGE("cmd.usage.setworld", "/jump setworld <jump> <world name>"),
    SPAWN_UPDATED("editor.updated.spawn", "Spawn updated for %s"),
    START_UPDATED("editor.updated.start", "Start position updated for %s"),
    SUCCESS_WORLD_SET("cmd.success.setworld", "World of %s has been set to %s!"),
    TOP_INVENTORY_TITLE("inventory.top.name", "TOP 10"),
    TOP_SCORE_LORE("inventory.top.score.lore", "%1$s\n%3$02d'%4$02d''%5$03d"),
    TOP_SCORE_TITLE("inventory.top.score.name", "TOP %2$d"),
    UNKNOWN_COMMAND("unknown_command", ChatColor.RED + "Unknown Command"),
    UNKNOWN_WORLD("unknown_world", "There isn't any world with the name '%s'\n available worlds are %s"),
    USAGE("usage", ChatColor.RED + "Usage: %s");


    public static final Locale DEFAULT_LANG = Locale.FRANCE; // may be change in config later
    public static final Locale[] AVAILABLE_LOCALES = {Locale.FRANCE};
    public static final String LOCALES_FOLDER = "locales";
    private static final Pattern indexed = Pattern.compile(".*\\{[^}]+}.*");
    private static final Pattern indexable = Pattern.compile("\\{}");

    /**
     * The key to access to the message in the translations file
     */
    private final String key;
    /**
     * The default message to use if no translation is available in the file
     */
    private final String defaultMessage;
    /**
     * The translated message extracted from the translation file
     */
    private String translatedMessage = null;
    /**
     * The message for the translated message
     */
    private MessageFormat message = null;

    /**
     * Initialize the translator base on the default language of the plugin
     * @param plugin the Bukkit plugin to get resources from
     */
    public static void init(JumpPlugin plugin) throws LocaleInitializationException {
        Locale configuredLocale = plugin.getConfigProvider().get(Key.LOCALE);
        boolean available = Arrays.asList(AVAILABLE_LOCALES).contains(configuredLocale);

        if (!available) plugin.getLogger().warning(
                configuredLocale.getDisplayLanguage(Locale.ENGLISH) +
                        "is not available, switching to " +
                        DEFAULT_LANG.getDisplayLanguage(Locale.ENGLISH));
        else plugin.getLogger().info("Using translations for " + configuredLocale.getDisplayLanguage(Locale.ENGLISH));

        Locale locale = available ? configuredLocale : DEFAULT_LANG;
        String fileName = localeToFileName(locale);

        try (InputStream resource = plugin.getResource(fileName)) {
            File file = new File(plugin.getDataFolder(), fileName);
            Validate.notNull(resource, String.format("Unable to find language file for '%s'", locale));
            FileConfiguration lang = YamlConfiguration.loadConfiguration(file);
            lang.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(resource, Charsets.UTF_8)));
            Arrays.stream(Text.values()).forEach(text -> text.init(lang));
        } catch (Exception exception) {
            throw new LocaleInitializationException(locale, exception);
        }
    }

    /**
     * Create a new text message with information to retrieve the translation from the file
     * @param key the key of the translation in the translations file
     * @param defaultMessage the message to return if the translation is missing, eg. the international version
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

    private void init(FileConfiguration lang) {
        if (this.key == null) this.translatedMessage = this.defaultMessage;
        else if (lang.isList(this.key)) this.translatedMessage = Optional.of(lang.getStringList(this.key))
                .filter(strings -> !strings.isEmpty())
                .map(strings -> String.join(StringUtils.LF, strings))
                .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                .orElse(this.defaultMessage);
        else this.translatedMessage = Optional.ofNullable(lang.getString(this.key))
                    .map(s -> ChatColor.translateAlternateColorCodes('&', s))
                    .orElse(this.defaultMessage);

        try {
            if (this.translatedMessage.contains("{}")) {
                if (!indexed.matcher(this.translatedMessage).matches()) {
                    Matcher matcher = indexable.matcher(this.translatedMessage);
                    StringBuffer sb = new StringBuffer();
                    for (int i=0; matcher.find(); i++) {
                        matcher.appendReplacement(sb, "{" + i + "}");
                    }
                    matcher.appendTail(sb);
                    this.message = new MessageFormat(sb.toString());
                } else {
                    throw new IllegalArgumentException(this.key + " mixes non-indexed ({}) and indexed ({n}) parameters");
                }
            } else {
                this.message = new MessageFormat(this.translatedMessage);
            }
        } catch (IllegalArgumentException exception) {
            if (exception.getCause() != null) {
                throw new IllegalArgumentException("Could not parse " + this.key, exception.getCause());
            } else {
                throw new IllegalArgumentException("Could not parse " + this.key, exception);
            }
        }
    }

    /**
     * Translate this message and return the translated version
     * @return the translated message
     */
    public String get() {
        Validate.notNull(this.translatedMessage, "Translations are not initialised");
        return this.translatedMessage;
    }

    public String get(Object... args) {
        return this.message.format(args);
    }

    @Override
    public String toString() {
        return this.get();
    }

    /*
     * Static methods
     */

    public static String localeToFileName(Locale locale) {
        return LOCALES_FOLDER + "/" + locale.getLanguage() + "-" + locale.getCountry() + ".yml";
    }

    private static File localeToFile(JumpPlugin plugin, Locale locale) {
        return new File(plugin.getDataFolder(), localeToFileName(locale));
    }
}
