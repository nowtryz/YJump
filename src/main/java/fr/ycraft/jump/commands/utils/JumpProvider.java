package fr.ycraft.jump.commands.utils;

import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.manager.JumpManager;
import lombok.RequiredArgsConstructor;
import net.nowtryz.mcutils.command.ArgProvider;

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
}
