package fr.ycraft.jump.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class TimeScore {
    private final long duration;
    private final long date;

    public TimeScore(long duration) {
        this(duration, System.currentTimeMillis());
    }

    public TimeScore(long duration, long date) {
        this.date = date;
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "TimeScore[" + duration + '@' + date + ']';
    }
}
