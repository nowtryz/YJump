package fr.ycraft.jump.commands.utils;

import fr.ycraft.jump.manager.EditorsManager;
import fr.ycraft.jump.sessions.JumpEditor;
import lombok.RequiredArgsConstructor;
import net.nowtryz.mcutils.command.ContextProvider;
import net.nowtryz.mcutils.command.SenderType;
import net.nowtryz.mcutils.command.contexts.ExecutionContext;
import org.bukkit.entity.Player;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor_={@Inject})
public class EditorProvider implements ContextProvider<JumpEditor> {
    private final EditorsManager manager;

    @Override
    public Class<JumpEditor> getProvidedClass() {
        return JumpEditor.class;
    }

    @Override
    public SenderType getTarget() {
        return SenderType.PLAYER;
    }

    @Override
    public JumpEditor provide(ExecutionContext context) {
        return this.manager.getEditor((Player) context.getSender()).orElse(null);
    }
}
