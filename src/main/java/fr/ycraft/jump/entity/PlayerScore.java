package fr.ycraft.jump.entity;

import lombok.Getter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.bukkit.util.NumberConversions.toLong;

@Getter
@ToString
@SerializableAs("score")
public class PlayerScore implements ConfigurationSerializable {
    private static final String PLAYER = "player", SCORE = "score", DATE = "date";
    private final OfflinePlayer player;
    private final TimeScore score;

    public PlayerScore(@NotNull OfflinePlayer player, long millis) {
        this(player, new TimeScore(millis));
    }

    public PlayerScore(OfflinePlayer player, TimeScore score) {
        this.player = player;
        this.score = score;
    }

    public long getMillis() {
        return this.score.getDuration();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();

        data.put(PLAYER, this.player.getUniqueId().toString());
        data.put(SCORE, this.score.getDuration());
        data.put(DATE, this.score.getDate());

        return data;
    }

    /**
     * Required method for deserialization
     *
     * @param args map to deserialize
     * @return deserialized score
     * @throws IllegalArgumentException if the world don't exists
     * @see ConfigurationSerializable
     */
    public static PlayerScore deserialize(Map<String, Object> args) {
        return Optional.ofNullable(args.get(PLAYER))
            .map(Object::toString)
            .map(UUID::fromString)
            .map(Bukkit::getOfflinePlayer)
            .map(player -> new PlayerScore(
                player,
                args.containsKey(DATE) ?
                    new TimeScore(toLong(args.get(SCORE)), toLong(args.get(DATE))) :
                    new TimeScore(toLong(args.get(SCORE)))
            )).orElse(null);
    }
}
