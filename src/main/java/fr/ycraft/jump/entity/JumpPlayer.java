package fr.ycraft.jump.entity;

import com.google.common.collect.ImmutableList;
import lombok.Getter;

import java.util.*;


@Getter
public class JumpPlayer extends HashMap<Jump, List<TimeScore>> {
    private final UUID id;
    private final String name;

    public JumpPlayer(UUID id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public JumpPlayer(UUID id, String name, Map<? extends Jump, ? extends List<TimeScore>> scores) {
        super(scores);
        this.id = id;
        this.name = name;
    }

    @Override
    public List<TimeScore> get(Object jump) {
        return Optional.ofNullable(super.get(jump))
                .map(t -> ImmutableList.sortedCopyOf(Comparator.comparingLong(TimeScore::getDuration), t))
                .orElseGet(ImmutableList::of);
    }

    public void put(Jump jump, long score) {
        this.put(jump, new TimeScore(score));
    }

    public void put(Jump jump, TimeScore score) {
        this.computeIfAbsent(jump, j -> new ArrayList<>()).add(score);
    }

    @Override
    public List<TimeScore> put(Jump jump, List<TimeScore> scores) {
        throw new UnsupportedOperationException();
    }

    public boolean hasCompleted(Jump jump) {
        return this.containsKey(jump);
    }
}
