package io.github.a5b84.helditeminfo.mixin.item;

import io.github.a5b84.helditeminfo.GenericTooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import io.github.a5b84.helditeminfo.Util;
import net.minecraft.item.CrossbowItem;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin implements GenericTooltipAppender {

    @Override
    public boolean heldItemInfo_shouldAppendTooltip() {
        return config.showCrossbowProjectiles();
    }

    @Override
    public List<Text> heldItemInfo_postProcess(TooltipBuilder builder, List<Text> tooltip) {
        Util.setAllToDefaultColor(tooltip);
        return tooltip;
    }
}
