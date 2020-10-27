package fr.ycraft.jump.inventories;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.Text;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.entity.PlayerScore;
import fr.ycraft.jump.injection.Nullable;
import fr.ycraft.jump.templates.Pattern;
import fr.ycraft.jump.templates.TemplatedGuiBuilder;
import net.nowtryz.mcutils.api.Gui;
import net.nowtryz.mcutils.builders.ItemBuilder;
import net.nowtryz.mcutils.inventory.AbstractGui;
import org.bukkit.entity.Player;

import javax.inject.Named;
import java.util.LinkedList;


public class BestScoresInventory extends AbstractGui<JumpPlugin> {
    @Inject
    public BestScoresInventory(
            JumpPlugin plugin,
            @Named("LEADERBOARD") Pattern pattern,
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
            builder.hookProvider("place" + (i + 1), b -> b.setDisplayName(Text.EMPTY_SCORE.get()));
        }
    }

    public interface Factory {
        BestScoresInventory create(Player player, Jump jump, @Nullable Gui gui);
    }
}
