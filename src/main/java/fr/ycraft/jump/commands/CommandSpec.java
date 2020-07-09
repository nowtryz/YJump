package fr.ycraft.jump.commands;

import fr.ycraft.jump.Text;

import java.util.Optional;

public enum CommandSpec {
    JUMP("jump", Perm.PLAY, Text.DESC_JUMP),
    JUMPS("jumps", Perm.LIST, Text.DESC_JUMPS),
    CHECKPOINT("checkpoint", Perm.PLAY, Text.DESC_CHECKPOINT),
    ADD_CHECKPOINT(JUMP, "addcheckpoint", Perm.EDIT, Text.DESC_ADD_CHECKPOINT),
    CREATE(JUMP, "create", Perm.CREATE, Text.DESC_CREATE, Text.CREATE_USAGE),
    EDIT(JUMP, "edit", Perm.EDIT, Text.DESC_EDIT, Text.EDIT_USAGE),
    DELETE(JUMP, "delete", Perm.EDIT, Text.DESC_DELETE, Text.DELETE_USAGE),
    HELP(JUMP, "help", Perm.HELP, Text.DESC_HELP),
    SAVE(JUMP, "save", Perm.EDIT, Text.DESC_SAVE, Text.SAVE_USAGE),
    LIST(JUMP, "list", Perm.ADMIN_LIST, Text.DESC_LIST, Text.LIST_USAGE),
    LEAVE(JUMP, "leave", Perm.PLAY, Text.DESC_LEAVE, Text.LEAVE_USAGE),
    SET_SPAWN(JUMP, "setspawn", Perm.EDIT, Text.DESC_SET_SPAWN),
    SET_START(JUMP, "setstart", Perm.EDIT, Text.DESC_SET_START),
    SET_ITEM(JUMP, "setitem", Perm.EDIT, Text.DESC_SET_ITEM),
    SET_END(JUMP, "setend", Perm.EDIT, Text.DESC_SET_END),
    SET_DESCRIPTION(JUMP, "setdesc", Perm.EDIT, Text.DESC_SET_DESCRIPTION, Text.SET_DESCRIPTION_USAGE),
    RENAME(JUMP, "rename", Perm.EDIT, Text.DESC_RENAME, Text.RENAME_USAGE),
    INFO(JUMP, "info", Perm.EDIT, Text.DESC_INFO),
    RELOAD(JUMP, "reload", Perm.RELOAD, Text.DESC_RELOAD);

    public final String label;
    public final Perm permission;
    public final Text description;
    private final Text usage;
    private final CommandSpec parent;

    CommandSpec(CommandSpec parent, String label, Perm permission, Text description, Text usage) {
        this.parent = parent;
        this.label = label;
        this.permission = permission;
        this.description = description;
        this.usage = usage;
    }

    CommandSpec(CommandSpec parent, String label, Perm permission, Text description) {
        this.parent = parent;
        this.label = label;
        this.permission = permission;
        this.description = description;
        this.usage = null;
    }

    CommandSpec(String label, Perm permission, Text description, Text usage) {
        this.parent = null;
        this.label = label;
        this.permission = permission;
        this.description = description;
        this.usage = usage;
    }

    CommandSpec(String label, Perm permission, Text description) {
        this.parent = null;
        this.label = label;
        this.permission = permission;
        this.description = description;
        this.usage = null;
    }

    public String getUsage() {
        return Optional.ofNullable(usage)
                .map(Text::get)
                .orElse(this.generateUsage());
    }

    private String generateUsage() {
        if (this.parent == null) return "/" + this.label;
        else return this.parent.generateUsage() + " " + this.label;
    }
}
