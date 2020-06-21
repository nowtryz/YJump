package fr.ycraft.jump.manager;

import fr.ycraft.jump.JumpEditor;
import fr.ycraft.jump.JumpGame;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class EditorsManager extends AbstractManager {
    private final Map<Jump, JumpEditor> editors = new LinkedHashMap<>();
    private final Map<Player, JumpEditor> playersInEditors = new LinkedHashMap<>();

    public EditorsManager(JumpPlugin plugin) {
        super(plugin);
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
    }

    public Optional<JumpEditor> getEditor(Jump jump) {
        return Optional.ofNullable(this.editors.get(jump));
    }

    public void enter(Jump jump, Player player) {
        JumpEditor editor = this.editors.get(jump);

        // If there is no editor for this jump we create a new one
        if (editor == null) {
            editor = new JumpEditor(this.plugin, jump);
            this.editors.put(jump, editor);
        }

        this.playersInEditors.put(player, editor);
        this.plugin.getGameManager().getGame(player).ifPresent(JumpGame::close);
        editor.join(player);
    }

    public void close() {
        this.editors.values().forEach(JumpEditor::close);
        this.editors.clear();
        this.playersInEditors.clear();
    }
}
