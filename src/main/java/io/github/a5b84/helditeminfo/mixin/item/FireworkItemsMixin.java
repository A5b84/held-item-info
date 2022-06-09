package io.github.a5b84.helditeminfo.mixin.item;

import io.github.a5b84.helditeminfo.GenericTooltipAppender;
import net.minecraft.item.FireworkChargeItem;
import net.minecraft.item.FireworkItem;
import org.spongepowered.asm.mixin.Mixin;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

@Mixin({FireworkItem.class, FireworkChargeItem.class})
public abstract class FireworkItemsMixin implements GenericTooltipAppender {

    @Override
    public boolean heldItemInfo_shouldAppendTooltip() {
        return config.showFireworkAttributes();
    }

}
