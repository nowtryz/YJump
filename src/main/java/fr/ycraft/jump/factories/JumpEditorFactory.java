package fr.ycraft.jump.factories;

import fr.ycraft.jump.sessions.JumpEditor;
import fr.ycraft.jump.entity.Jump;

public interface JumpEditorFactory {
    JumpEditor create(Jump jump);
}
