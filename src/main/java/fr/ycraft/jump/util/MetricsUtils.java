package fr.ycraft.jump.util;

import fr.ycraft.jump.JumpPlugin;

import java.util.concurrent.Callable;

public class MetricsUtils {
    public static void init(JumpPlugin plugin) {
        org.bstats.bukkit.Metrics metrics = new org.bstats.bukkit.Metrics(plugin, 8322);
        metrics.addCustomChart(new org.bstats.bukkit.Metrics.SingleLineChart("registered_jumps", registeredJumps(plugin)));
    }

    private static Callable<Integer> registeredJumps(JumpPlugin plugin) {
        return () -> plugin.getJumpManager().getJumps().size();
    }
}
