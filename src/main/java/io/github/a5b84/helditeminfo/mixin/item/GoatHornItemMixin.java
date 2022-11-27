package io.github.a5b84.helditeminfo.mixin.item;

import io.github.a5b84.helditeminfo.GenericTooltipAppender;
import net.minecraft.item.GoatHornItem;
import org.spongepowered.asm.mixin.Mixin;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

@Mixin(GoatHornItem.class)
public abstract class GoatHornItemMixin implements GenericTooltipAppender {

    @Override
    public boolean heldItemInfo_shouldAppendTooltip() {
        return config.showGoatHornInstrument();
    }

}
