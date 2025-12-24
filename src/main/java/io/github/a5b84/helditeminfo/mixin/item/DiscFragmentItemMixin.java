package io.github.a5b84.helditeminfo.mixin.item;

import io.github.a5b84.helditeminfo.GenericTooltipAppender;
import io.github.a5b84.helditeminfo.HeldItemInfo;
import net.minecraft.world.item.DiscFragmentItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DiscFragmentItem.class)
public abstract class DiscFragmentItemMixin implements GenericTooltipAppender {

  @Override
  public boolean heldItemInfo_shouldAppendTooltip() {
    return HeldItemInfo.config.showMusicDiscDescription();
  }
}
