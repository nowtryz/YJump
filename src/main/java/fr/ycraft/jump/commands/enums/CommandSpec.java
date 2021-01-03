package fr.ycraft.jump.commands.enums;

import fr.ycraft.jump.enums.Text;

import java.util.Optional;

public enum CommandSpec {
    JUMP("jump", Perm.PLAY, Text.DESC_JUMP),
    JUMPS("jumps [jump]", Perm.LIST, Text.DESC_JUMPS),
    CHECKPOINT("checkpoint", Perm.PLAY, Text.DESC_CHECKPOINT),
    ADD_CHECKPOINT("jump addcheckpoint", Perm.EDIT, Text.DESC_ADD_CHECKPOINT),
    CREATE("jump create <name>", Perm.CREATE, Text.DESC_CREATE, Text.CREATE_USAGE),
    EDIT("jump edit <jump>", Perm.EDIT, Text.DESC_EDIT, Text.EDIT_USAGE),
    DELETE("jump delete <jump>", Perm.EDIT, Text.DESC_DELETE, Text.DELETE_USAGE),
    HELP("jump help", Perm.HELP, Text.DESC_HELP),
    SAVE("jump save", Perm.EDIT, Text.DESC_SAVE),
    SET_WORLD("jump setworld <jump> <world>", Perm.EDIT, Text.DESC_SET_WORLD, Text.SET_WORLD_USAGE),
    LIST("jump list", Perm.ADMIN_LIST, Text.DESC_LIST, Text.LIST_USAGE),
    LEAVE("jump leave", Perm.PLAY, Text.DESC_LEAVE, Text.LEAVE_USAGE),
    SET_SPAWN("jump setspawn", Perm.EDIT, Text.DESC_SET_SPAWN),
    SET_START("jump setstart", Perm.EDIT, Text.DESC_SET_START),
    SET_ITEM("jump setitem", Perm.EDIT, Text.DESC_SET_ITEM),
    SET_END("jump setend", Perm.EDIT, Text.DESC_SET_END),
    SET_DESCRIPTION("jump setdesc <desc>", Perm.EDIT, Text.DESC_SET_DESCRIPTION, Text.SET_DESCRIPTION_USAGE),
    RENAME("jump rename <name>", Perm.EDIT, Text.DESC_RENAME, Text.RENAME_USAGE),
    INFO("jump info [jump]", Perm.ADMIN_LIST, Text.DESC_INFO),
    RELOAD("jump reload", Perm.RELOAD, Text.DESC_RELOAD);

    public final String usage;
    public final String permission;
    public final Text description;
    public final Text usageTranslation;


    CommandSpec(String usage, String permission, Text description) {
        this.usage = usage;
        this.permission = permission;
        this.description = description;
        this.usageTranslation = null;
    }

    CommandSpec(String usage, String permission, Text description, Text usageTranslation) {
        this.usage = usage;
        this.permission = permission;
        this.description = description;
        this.usageTranslation = usageTranslation;
    }

    public String getUsage() {
        return Optional.ofNullable(this.usageTranslation)
                .map(Text::get)
                .orElseGet(() -> '/' + this.usage);
    }
}
