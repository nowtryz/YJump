package fr.ycraft.jump.listeners;

import fr.ycraft.jump.JumpPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class AbstractListener implements Listener {
    protected JumpPlugin plugin;

    public AbstractListener(JumpPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    public void unRegister() {
        HandlerList.unregisterAll(this);
    }
}
