package fr.ycraft.jump.inventories;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.PlayerScore;
import fr.ycraft.jump.enums.Patterns;
import fr.ycraft.jump.enums.Text;
import fr.ycraft.jump.injection.Patterned;
import net.nowtryz.mcutils.api.Gui;
import net.nowtryz.mcutils.builders.ItemBuilder;
import net.nowtryz.mcutils.injection.Nullable;
import net.nowtryz.mcutils.inventory.AbstractGui;
import net.nowtryz.mcutils.templating.Pattern;
import net.nowtryz.mcutils.templating.TemplatedGuiBuilder;
import org.bukkit.entity.Player;

import java.util.LinkedList;


public class BestScoresInventory extends AbstractGui<JumpPlugin> {
    @Inject
    public BestScoresInventory(
            JumpPlugin plugin,
            @Patterned(Patterns.LEADERBOARD) Pattern pattern,
            @Assisted Player player,
            @Assisted Jump jump,
            @Assisted Gui back) {
        super(plugin, player, back);



        TemplatedGuiBuilder builder = pattern.builder(this)
                .name(Text.TOP_INVENTORY_TITLE.get(jump.getName()))
                // if an inventory can handle a back arrow
                .hookBack("back", b -> b.setDisplayName(Text.BACK));

        LinkedList<PlayerScore> bestScores = new LinkedList<>(jump.getBestScores());
        for (int i = 0; i < 10; i++) if (i < bestScores.size()) {
            PlayerScore score = bestScores.get(i);
            builder.hookItem("place" + (i + 1), ItemBuilder.skullForPlayer(score.getPlayer())
                    .setDisplayName(Text.TOP_SCORE_TITLE,
                            score.getPlayer().getName(),
                            i + 1,
                            score.getScore().getMinutes(),
                            score.getScore().getSeconds(),
                            score.getScore().getMillis())
                    .setLore(Text.TOP_SCORE_LORE,
                            score.getPlayer().getName(),
                            i + 1,
                            score.getScore().getMinutes(),
                            score.getScore().getSeconds(),
                            score.getScore().getMillis())
                    .build());
        } else {
            builder.fallback("place" + (i + 1), b -> b.setDisplayName(Text.EMPTY_SCORE.get()));
        }
    }

    public interface Factory {
        BestScoresInventory create(Player player, Jump jump, @Nullable Gui gui);
    }
}
