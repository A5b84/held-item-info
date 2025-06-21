package io.github.a5b84.helditeminfo;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link TooltipAppender} that adds the tooltip using {@link Item#appendTooltip}.
 */
public interface GenericTooltipAppender extends TooltipAppender {

    default void heldItemInfo_appendTooltip(TooltipBuilder builder) {
        ItemStack stack = builder.getStack();
        Item item = stack.getItem();
        List<Text> tooltip = new ArrayList<>();
        item.appendTooltip(stack, builder.getTooltipContext(), builder.getDisplayComponent(), tooltip::add, TooltipType.Default.BASIC);
        tooltip = heldItemInfo_postProcess(builder, tooltip);
        builder.appendAll(tooltip);
    }

    default List<Text> heldItemInfo_postProcess(TooltipBuilder builder, List<Text> tooltip) {
        return tooltip;
    }

}
