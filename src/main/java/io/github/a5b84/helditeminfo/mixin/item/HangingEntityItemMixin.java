package io.github.a5b84.helditeminfo.mixin.item;

import io.github.a5b84.helditeminfo.GenericTooltipAppender;
import io.github.a5b84.helditeminfo.HeldItemInfo;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import io.github.a5b84.helditeminfo.Util;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.HangingEntityItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HangingEntityItem.class)
public abstract class HangingEntityItemMixin implements GenericTooltipAppender {

  @Override
  public boolean heldItemInfo_shouldAppendTooltip() {
    return HeldItemInfo.config.showPaintingDescription();
  }

  @Override
  public List<Component> heldItemInfo_postProcess(TooltipBuilder builder, List<Component> tooltip) {
    Util.setAllToDefaultColor(tooltip);
    return tooltip;
  }
}
