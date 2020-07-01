package fr.ycraft.jump.manager;

import fr.ycraft.jump.JumpPlugin;

/**
 * Managers are used to store object between reloads
 */
public abstract class AbstractManager {
    protected final JumpPlugin plugin;

    public AbstractManager(JumpPlugin plugin) {
        this.plugin = plugin;
    }
}
