package fr.ycraft.jump.listeners;

import fr.ycraft.jump.JumpPlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class AbstractListener implements Listener {
    protected JumpPlugin plugin;
    @Getter
    private boolean registered = false;

    public AbstractListener(JumpPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        if (this.registered) this.plugin
                .getLogger()
                .warning(this.getClass().getName() + " registered but was previously registered");
        else {
            Bukkit.getPluginManager().registerEvents(this, this.plugin);
            this.registered = true;
        }
    }

    public void unRegister() {
        HandlerList.unregisterAll(this);
        this.registered = false;
    }
}
