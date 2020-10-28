package fr.ycraft.jump.manager;

import com.google.inject.Inject;
import fr.ycraft.jump.sessions.JumpEditor;
import fr.ycraft.jump.sessions.JumpGame;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.configuration.Config;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.factories.JumpEditorFactory;
import fr.ycraft.jump.listeners.EditorListener;
import org.bukkit.entity.Player;

import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class EditorsManager extends AbstractManager {
    private final Map<Jump, JumpEditor> editors = new LinkedHashMap<>();
    private final Map<Player, JumpEditor> playersInEditors = new LinkedHashMap<>();

    private final EditorListener listener;
    private final GameManager gameManager;
    private final JumpEditorFactory factory;

    @Inject
    public EditorsManager(JumpPlugin plugin, Config config, GameManager gameManager, JumpEditorFactory factory) {
        super(plugin);
        this.listener = new EditorListener(plugin, config, this);
        this.gameManager = gameManager;
        this.factory = factory;
    }

    public Map<Jump, JumpEditor> getEditors() {
        return new LinkedHashMap<>(editors);
    }

    public Map<Player, JumpEditor> getPlayersInEditors() {
        return new LinkedHashMap<>(playersInEditors);
    }

    public Optional<JumpEditor> getEditor(Player player) {
        return Optional.ofNullable(this.playersInEditors.get(player));
    }

    public boolean isInEditor(Player player) {
        return this.playersInEditors.containsKey(player);
    }

    public void leave(Player player) {
        JumpEditor editor = this.playersInEditors.get(player);
        this.playersInEditors.remove(player);
        editor.leave(player);

        if (editor.getPlayers().isEmpty()) {
            editor.close();
            this.editors.remove(editor.getJump(), editor);
        }

        if (this.editors.isEmpty()) this.listener.unregister();
    }

    public Optional<JumpEditor> getEditor(Jump jump) {
        return Optional.ofNullable(this.editors.get(jump));
    }

    public void enter(Jump jump, Player player) {
        // register listener if needed
        if (this.editors.isEmpty()) this.listener.register();

        JumpEditor editor = Optional.ofNullable(this.editors.get(jump))
                .orElseGet(() -> {
            JumpEditor instance = this.factory.create(jump);
            this.editors.put(jump, instance);
            return instance;
        });

        this.playersInEditors.put(player, editor);
        this.gameManager.getGame(player).ifPresent(JumpGame::close);
        editor.join(player);
    }

    public void close() {
        this.editors.values().forEach(JumpEditor::close);
        this.editors.clear();
        this.playersInEditors.clear();
    }
}
