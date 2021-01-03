package fr.ycraft.jump.commands.utils;

import fr.ycraft.jump.enums.Text;
import net.nowtryz.mcutils.command.ArgProvider;
import net.nowtryz.mcutils.command.CommandResult;
import net.nowtryz.mcutils.command.contexts.ExecutionContext;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.stream.Collectors;

public class WorldProvider implements ArgProvider<World> {
    @Override
    public Class<World> getProvidedClass() {
        return World.class;
    }

    @Override
    public World provide(String name) {
        return Bukkit.getWorld(name);
    }

    @Override
    public CommandResult onNull(ExecutionContext context, String argument) {
        Text.UNKNOWN_WORLD.send(context.getSender(), argument, Bukkit.getWorlds()
                .parallelStream()
                .map(World::getName)
                .collect(Collectors.joining(", ")));
        return CommandResult.FAILED;
    }
}
