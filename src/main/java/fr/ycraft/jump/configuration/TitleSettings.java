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

    public void send(Player player, String title, String subtitle) {
        if (!this.enabled) return;
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
}
