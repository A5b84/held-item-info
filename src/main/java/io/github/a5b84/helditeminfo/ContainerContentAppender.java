package io.github.a5b84.helditeminfo;

import com.google.common.collect.Iterables;
import io.github.a5b84.helditeminfo.mixin.ShulkerBoxBlockAccessor;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ContainerContentAppender {

    private ContainerContentAppender() {}

    /**
     * @see ShulkerBoxBlock#appendTooltip(ItemStack, Item.TooltipContext, List, TooltipType)
     */
    public static void appendContainerContent(TooltipBuilder builder) {
        if (builder.stack.contains(DataComponentTypes.CONTAINER_LOOT)) {
            builder.append(ShulkerBoxBlockAccessor.getUnknownContentsText());
        }

        for (ContainerEntry innerStack : getContainerEntries(builder.stack)) {
            builder.append(() -> Text.translatable("container.shulkerBox.itemCount", innerStack.getName(), innerStack.getCount())
                    .formatted(TooltipBuilder.DEFAULT_COLOR));
        }
    }

    private static Iterable<? extends ContainerEntry> getContainerEntries(ItemStack containerStack) {
        Iterable<ItemStack> stacks = Collections.emptyList();

        ContainerComponent containerComponent = containerStack.get(DataComponentTypes.CONTAINER);
        if (containerComponent != null) {
            stacks = containerComponent.iterateNonEmpty();
        }

        BundleContentsComponent bundleContents = containerStack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundleContents != null) {
            stacks = Iterables.concat(stacks, bundleContents.iterate());
        }

        if (HeldItemInfo.config.mergeSimilarContainerItems()) {
            Map<Text, MergedContainerEntry> entries = new LinkedHashMap<>();

            for (ItemStack stack : stacks) {
                entries.compute(stack.getName(), (name, entry) -> {
                    if (entry == null) {
                        entry = new MergedContainerEntry(name);
                    }

                    entry.count += stack.getCount();
                    return entry;
                });
            }

            return entries.values();
        } else {
            return Iterables.transform(stacks, ItemStackContainerEntry::new);
        }
    }


    private static abstract class ContainerEntry {
        public abstract Text getName();
        public abstract int getCount();
    }

    private static class ItemStackContainerEntry extends ContainerEntry {
        private final ItemStack stack;

        public ItemStackContainerEntry(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public Text getName() {
            return stack.getName();
        }

        @Override
        public int getCount() {
            return stack.getCount();
        }
    }

    private static class MergedContainerEntry extends ContainerEntry {
        private final Text name;
        private int count = 0;

        private MergedContainerEntry(Text name) {
            this.name = name;
        }

        @Override
        public Text getName() {
            return name;
        }

        @Override
        public int getCount() {
            return count;
        }
    }
}
