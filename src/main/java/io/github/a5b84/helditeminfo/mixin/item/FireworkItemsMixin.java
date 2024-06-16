package io.github.a5b84.helditeminfo.mixin.item;

import io.github.a5b84.helditeminfo.GenericTooltipAppender;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.FireworkStarItem;
import org.spongepowered.asm.mixin.Mixin;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

@Mixin({FireworkRocketItem.class, FireworkStarItem.class})
public abstract class FireworkItemsMixin implements GenericTooltipAppender {

    @Override
    public boolean heldItemInfo_shouldAppendTooltip() {
        return config.showFireworkAttributes();
    }
}
