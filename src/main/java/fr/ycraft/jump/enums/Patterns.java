package fr.ycraft.jump.enums;

import fr.ycraft.jump.injection.Patterned;
import fr.ycraft.jump.injection.PatternedImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Patterns {
    ADMIN("admin.yml"),
    FALL_DISTANCE("fall_distance.yml"),
    INFO("info.yml"),
    LEADERBOARD("leaderboard.yml"),
    LIST("list.yml");

    public static final String FOLDER_NAME = "guis";

    private final String fileName;

    public Patterned annotation() {
        return new PatternedImpl(this);
    }
}
