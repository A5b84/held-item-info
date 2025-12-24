package io.github.a5b84.helditeminfo;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** {@link TooltipAppender} that adds the tooltip using {@link Item#appendHoverText}. */
@SuppressWarnings("deprecation")
public interface GenericTooltipAppender extends TooltipAppender {

  default void heldItemInfo_appendTooltip(TooltipBuilder builder) {
    ItemStack stack = builder.getStack();
    Item item = stack.getItem();
    List<Component> tooltip = new ArrayList<>();
    item.appendHoverText(
        stack,
        builder.getTooltipContext(),
        builder.getDisplayComponent(),
        tooltip::add,
        TooltipFlag.Default.NORMAL);
    tooltip = heldItemInfo_postProcess(builder, tooltip);
    builder.appendAll(tooltip);
  }

  default List<Component> heldItemInfo_postProcess(
      TooltipBuilder builder, List<Component> tooltip) {
    return tooltip;
  }
}
