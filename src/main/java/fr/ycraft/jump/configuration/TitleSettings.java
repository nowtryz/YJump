package fr.ycraft.jump.configuration;

import lombok.Builder;
import lombok.Value;
import org.bukkit.entity.Player;

@Value
@Builder
public class TitleSettings {
    boolean enabled;
    int fadeIn;
    int fadeOut;
    int stay;

    /**
     * Send a title with this settings if it's enabled
     * @param player the player to send the title to
     * @param title the title to send
     * @param subtitle the subtitle to send
     */
    public void send(Player player, String title, String subtitle) {
        if (!this.enabled) return;
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
}
