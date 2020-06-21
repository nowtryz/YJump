package fr.ycraft.jump.commands.jump;

import fr.ycraft.jump.JumpEditor;
import fr.ycraft.jump.JumpGame;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.commands.AbstractCommandImpl;
import fr.ycraft.jump.commands.CommandSpec;
import fr.ycraft.jump.entity.Jump;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeleteCommand extends AbstractCommandImpl {
    public DeleteCommand() {
        super(CommandSpec.DELETE, args -> args.length == 1);
    }

    @Override
    public boolean execute(JumpPlugin plugin, CommandSender sender, String[] args) {
        assert this.validator.test(args);
        Optional<Jump> jump = plugin.getJumpManager().getJump(args[0]);

        if (!jump.isPresent()) Text.JUMP_NOT_EXISTS.send(sender);
        jump.ifPresent(j -> this.deleteJump(plugin, sender, j));

        return true;
    }

    public void deleteJump(JumpPlugin plugin, CommandSender sender, Jump jump) {
        plugin.getEditorsManager().getEditor(jump).ifPresent(JumpEditor::close);
        plugin.getGameManager().getGames().stream().parallel()
                .filter(game -> game.getJump().equals(jump))
                .forEach(JumpGame::close);
        plugin.getJumpManager().delete(jump);
        Text.DELETED.send(sender, jump.getName());

        if (plugin.getConfigProvider().doesDeletePlates()) {
            Bukkit.getScheduler().runTask(plugin, () -> this.deleteAllPlates(jump));
        }
    }

    public void deleteAllPlates(Jump jump) {
        jump.getStart().map(Location::getBlock).ifPresent(block -> block.setType(Material.AIR));
        jump.getEnd().map(Location::getBlock).ifPresent(block -> block.setType(Material.AIR));
        jump.getCheckpoints().stream().map(Location::getBlock).forEach(block -> block.setType(Material.AIR));
    }

    @Override
    public List<String> tabComplete(JumpPlugin plugin, CommandSender sender, String[] args) {
        return plugin.getJumpManager()
                .getJumps()
                .values()
                .stream()
                .map(Jump::getName)
                .collect(Collectors.toList());
    }
}
