package fr.ycraft.jump.command.test;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.command.SenderType;
import fr.ycraft.jump.command.annotations.Arg;
import fr.ycraft.jump.command.annotations.Command;
import fr.ycraft.jump.command.annotations.Provides;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.manager.JumpManager;
import fr.ycraft.jump.storage.Storage;
import net.nowtryz.mcutils.command.CommandResult;
import net.nowtryz.mcutils.injection.PluginLogger;
import org.bukkit.command.CommandSender;

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class TestCommand {
    @Provides(target = "jump", provider = JumpProvider.class)
    @Command(value = "test jump <jump> info", type = SenderType.CONSOLE)
    static public CommandResult info(
            @Arg("jump") Jump jump,
            @Arg("jump") String name,
            @PluginLogger Logger logger,
            CommandSender sender) {
        if (jump == null) {
            sender.sendMessage(String.format("Unknown jump %s!", name));
            return CommandResult.FAILED;
        }

        logger.info(jump.toString());
        return CommandResult.SUCCESS;
    }

    @Provides(target = "jump", provider = JumpProvider.class)
    @Command(value = "test jump <jump> set name <name>", async = true)
    static public CommandResult test(
            @Arg("name") String name,
            @Arg("jump") Jump jump,
            @PluginLogger Logger logger,
            CommandSender sender,
            JumpManager manager,
            Storage storage,
            JumpPlugin plugin) {

        if (jump == null) {
            // say jump is unknown
            return CommandResult.INVALID_ARGUMENTS;
        }

        jump.setName(name);

        try {
            storage.storeJump(jump).thenRun(manager::updateJumpList).get();
            sender.sendMessage("Jump " + jump.getName() + " updated!");
            return CommandResult.SUCCESS;
        } catch (InterruptedException | ExecutionException e) {
            logger.severe("Unable to update jump: " + e.getMessage());
            if (!plugin.isProd()) e.printStackTrace();
            return CommandResult.INTERNAL_ERROR;
        }
    }
}
