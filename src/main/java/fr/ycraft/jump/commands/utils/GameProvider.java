package fr.ycraft.jump.commands.utils;

import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.manager.GameManager;
import fr.ycraft.jump.sessions.JumpGame;
import lombok.RequiredArgsConstructor;
import net.nowtryz.mcutils.command.CommandResult;
import net.nowtryz.mcutils.command.ContextProvider;
import net.nowtryz.mcutils.command.SenderType;
import net.nowtryz.mcutils.command.contexts.ExecutionContext;
import org.bukkit.entity.Player;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor_={@Inject})
public class GameProvider implements ContextProvider<JumpGame> {
    private final GameManager manager;

    @Override
    public Class<JumpGame> getProvidedClass() {
        return JumpGame.class;
    }

    @Override
    public SenderType getTarget() {
        return SenderType.PLAYER;
    }

    @Override
    public JumpGame provide(ExecutionContext context) {
        return this.manager.getGame((Player) context.getSender()).orElse(null);
    }

    @Override
    public CommandResult onNull(ExecutionContext context) {
        Text.ONLY_GAME_COMMAND.send(context.getSender());
        return CommandResult.FAILED;
    }
}
