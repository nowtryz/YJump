package fr.ycraft.jump.command.test;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.ycraft.jump.command.Provider;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.manager.JumpManager;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor_={@Inject})
public class JumpProvider implements Provider<Jump> {
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
