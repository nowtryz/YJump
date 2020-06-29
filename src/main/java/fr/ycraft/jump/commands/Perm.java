package fr.ycraft.jump.commands;

import org.bukkit.permissions.Permissible;

public enum Perm {
    /**
     * Gives access to admin commands
     */
    ADMIN("jump.admin.*"),
    /**
     * Give access to the <i>/jump</i> list command
     */
    ADMIN_LIST("jump.admin.list"),
    /**
     * Gives access to all jump permissions
     */
    ALL("jump.*"),
    /**
     * Gives access to the <i>/jump create</i> command and edit jumps
     */
    CREATE("jump.admin.create"),
    /**
     * Enables the player to edit jumps
     */
    EDIT("jump.admin.edit"),
    /**
     * Allow to places/destroy blocks while in editor
     */
    EDITOR_INTERACTIONS("jump.admin.editorinteractions"),
    /**
     * Enables player to fly in jumps
     */
    FLY("jump.fly"),
    /**
     * Give access to the <i>/jump help</i> command
     */
    HELP("jump.help"),
    /**
     * Gives access to the <i>/jumps</i> command
     */
    LIST("jump.list"),
    /**
     * Enables player to play a jump and leave jumps
     */
    PLAY("jump.play"),
    /**
     * Gives access to the <i>/jump reload</i> command
     */
    RELOAD("jump.admin.reload");

    private final String permission;

    Perm(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    @Override
    public String toString() {
        return permission;
    }

    public boolean isHeldBy(Permissible p) {
        return p.hasPermission(this.permission);
    }
}
