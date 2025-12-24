package io.github.a5b84.helditeminfo.mixin.item;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

import io.github.a5b84.helditeminfo.GenericTooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import io.github.a5b84.helditeminfo.Util;
import java.util.List;
import net.minecraft.item.DecorationItem;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DecorationItem.class)
public abstract class DecorationItemMixin implements GenericTooltipAppender {

  @Override
  public boolean heldItemInfo_shouldAppendTooltip() {
    return config.showPaintingDescription();
  }

  @Override
  public List<Text> heldItemInfo_postProcess(TooltipBuilder builder, List<Text> tooltip) {
    Util.setAllToDefaultColor(tooltip);
    return tooltip;
  }
}
