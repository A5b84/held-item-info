package io.github.a5b84.helditeminfo.mixin.item;

import io.github.a5b84.helditeminfo.GenericTooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import net.minecraft.item.PotionItem;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

@Mixin({PotionItem.class, TippedArrowItem.class}) // SplashPotionItem and LingeringPotionItem extend PotionItem
public abstract class ItemsWithPotionEffectsMixin implements GenericTooltipAppender {

    @Override
    public boolean heldItemInfo_shouldAppendTooltip() {
        return config.showPotionEffects();
    }

    @Override
    public List<Text> postProcess(TooltipBuilder builder, List<Text> tooltip) {
        // Remove the 'When applied: ...' lines (everything after the first blank line)
        int i = 0;
        for (Text line : tooltip) {
            if (line instanceof LiteralText && line.asString().equals("")) {
                tooltip = tooltip.subList(0, i);
                break;
            }
            i++;
        }

        return tooltip;
    }

}
