package fr.ycraft.jump.entity;

import fr.ycraft.jump.Text;
import org.bukkit.command.CommandSender;

public class TimeScore {
    private final long duration, millis, seconds, minutes;

    public TimeScore(long duration) {
        this.duration = duration;
        this.millis = duration % 1000;
        this.seconds = (duration / 1000) % 60;
        this.minutes = duration / 60000;
    }

    public long getDuration() {
        return duration;
    }

    public long getMillis() {
        return millis;
    }

    public long getSeconds() {
        return seconds;
    }

    public long getMinutes() {
        return minutes;
    }

    public void sendText(CommandSender target, Text text) {
        text.send(target, this.minutes, this.seconds, this.millis);
    }

    public String getText(Text text) {
        return text.get(this.minutes, this.seconds, this.millis);
    }
}
