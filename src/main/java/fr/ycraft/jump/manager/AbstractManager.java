package fr.ycraft.jump.manager;

import fr.ycraft.jump.JumpPlugin;

public abstract class AbstractManager {
    protected final JumpPlugin plugin;

    public AbstractManager(JumpPlugin plugin) {
        this.plugin = plugin;
    }
}
