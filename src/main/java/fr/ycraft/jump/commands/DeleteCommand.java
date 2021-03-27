package fr.ycraft.jump.commands;

import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.commands.enums.Perm;
import fr.ycraft.jump.commands.utils.JumpCompleter;
import fr.ycraft.jump.commands.utils.JumpProvider;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.configuration.Key;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.manager.EditorsManager;
import fr.ycraft.jump.manager.GameManager;
import fr.ycraft.jump.manager.JumpManager;
import fr.ycraft.jump.sessions.JumpEditor;
import fr.ycraft.jump.sessions.JumpGame;
import lombok.RequiredArgsConstructor;
import net.nowtryz.mcutils.command.CommandResult;
import net.nowtryz.mcutils.command.SenderType;
import net.nowtryz.mcutils.command.annotations.Arg;
import net.nowtryz.mcutils.command.annotations.Command;
import net.nowtryz.mcutils.command.annotations.Completer;
import net.nowtryz.mcutils.command.annotations.ProvidesArg;
import net.nowtryz.mcutils.command.contexts.CompletionContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import java.util.List;

@RequiredArgsConstructor(onConstructor_={@Inject})
public class DeleteCommand {
    private final JumpPlugin plugin;
    private final EditorsManager editorsManager;
    private final GameManager gameManager;
    private final JumpManager jumpManager;
    private final JumpCompleter completer;
    private final Config config;

    @Command(value = "jump delete <jump>", permission = Perm.EDIT)
    @ProvidesArg(target = "jump", provider = JumpProvider.class, ignoreNulls = true)
    public CommandResult execute(CommandSender sender, @Arg("jump") Jump jump) {
        this.editorsManager.getEditor(jump).ifPresent(JumpEditor::close);
        this.gameManager.getGames().stream().parallel()
                .filter(game -> game.getJump().equals(jump))
                .forEach(JumpGame::close);
        this.jumpManager.delete(jump);
        Text.DELETED.send(sender, jump.getName());

        if (this.config.get(Key.DELETE_PLATES)) {
            Bukkit.getScheduler().runTask(this.plugin, () -> this.deleteAllPlates(jump));
        }

        return CommandResult.SUCCESS;
    }

    public void deleteAllPlates(Jump jump) {
        jump.getStart().map(Location::getBlock).ifPresent(block -> block.setType(Material.AIR));
        jump.getEnd().map(Location::getBlock).ifPresent(block -> block.setType(Material.AIR));
        jump.getCheckpoints().stream().map(Location::getBlock).forEach(block -> block.setType(Material.AIR));
    }

    @Completer("jump delete <jump>")
    public List<String> tabComplete(CompletionContext context) {
        return this.completer.tabComplete(context);
    }
}
