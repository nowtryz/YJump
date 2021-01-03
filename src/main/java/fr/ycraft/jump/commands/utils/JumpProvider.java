package fr.ycraft.jump.commands.utils;

import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.manager.JumpManager;
import lombok.RequiredArgsConstructor;
import net.nowtryz.mcutils.command.ArgProvider;
import net.nowtryz.mcutils.command.CommandResult;
import net.nowtryz.mcutils.command.contexts.ExecutionContext;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor_={@Inject})
public class JumpProvider implements ArgProvider<Jump> {
    private final JumpManager manager;

    @Override
    public Class<Jump> getProvidedClass() {
        return Jump.class;
    }

    @Override
    public Jump provide(String argument) {
        return this.manager.getJump(argument).orElse(null);
    }

    @Override
    public CommandResult onNull(ExecutionContext context, String argument) {
        Text.JUMP_NOT_EXISTS.send(context.getSender());
        return CommandResult.FAILED;
    }
}
