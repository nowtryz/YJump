package fr.ycraft.jump.entity;

import fr.ycraft.jump.enums.Text;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.command.CommandSender;

@Getter
@ToString
@EqualsAndHashCode(exclude = {"millis", "seconds", "minutes"})
public class TimeScore {
    private final long duration;
    private final long millis;
    private final long seconds;
    private final long minutes;
    private final long date;

    public TimeScore(long duration) {
        this(duration, System.currentTimeMillis());
    }

    public TimeScore(long duration, long date) {
        this.date = date;
        this.duration = duration;
        this.millis = duration % 1000;
        this.seconds = (duration / 1000) % 60;
        this.minutes = duration / 60000;
    }

    public void sendText(CommandSender target, Text text) {
        text.send(target, this.minutes, this.seconds, this.millis);
    }

    public String getText(Text text) {
        return text.get(this.minutes, this.seconds, this.millis);
    }
}
