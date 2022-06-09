package io.github.a5b84.helditeminfo.mixin.item;

import io.github.a5b84.helditeminfo.GenericTooltipAppender;
import net.minecraft.item.BannerPatternItem;
import org.spongepowered.asm.mixin.Mixin;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

@Mixin(BannerPatternItem.class)
public abstract class BannerPatternItemMixin implements GenericTooltipAppender {

    @Override
    public boolean heldItemInfo_shouldAppendTooltip() {
        return config.showBannerPatternName();
    }

}
