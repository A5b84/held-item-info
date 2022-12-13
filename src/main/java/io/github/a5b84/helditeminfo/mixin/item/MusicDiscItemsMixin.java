package io.github.a5b84.helditeminfo.mixin.item;

import io.github.a5b84.helditeminfo.GenericTooltipAppender;
import net.minecraft.item.DiscFragmentItem;
import net.minecraft.item.MusicDiscItem;
import org.spongepowered.asm.mixin.Mixin;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

@Mixin({DiscFragmentItem.class, MusicDiscItem.class})
public abstract class MusicDiscItemsMixin implements GenericTooltipAppender {

    @Override
    public boolean heldItemInfo_shouldAppendTooltip() {
        return config.showMusicDiscDescription();
    }

}