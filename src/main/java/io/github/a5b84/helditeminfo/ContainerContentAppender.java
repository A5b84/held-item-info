package io.github.a5b84.helditeminfo;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

public final class ContainerContentAppender {

  private ContainerContentAppender() {}

  /**
   * @see ItemContainerContents#addToTooltip(Item.TooltipContext, Consumer, TooltipFlag,
   *     DataComponentGetter)
   */
  public static void appendContainerContent(TooltipBuilder builder) {
    builder.appendComponent(DataComponents.CONTAINER_LOOT);

    for (ContainerEntry innerStack : getContainerEntries(builder)) {
      builder.append(
          () ->
              Component.translatable(
                      "item.container.item_count", innerStack.getName(), innerStack.getCount())
                  .withStyle(TooltipBuilder.DEFAULT_COLOR));
    }
  }

  private static Iterable<? extends ContainerEntry> getContainerEntries(TooltipBuilder builder) {
    Iterable<ItemStack> stacks = Collections.emptyList();

    Optional<ItemContainerContents> containerComponent =
        builder.getComponentForDisplay(DataComponents.CONTAINER);
    if (containerComponent.isPresent()) {
      stacks = containerComponent.get().nonEmptyItems();
    }

    Optional<BundleContents> bundleContents =
        builder.getComponentForDisplay(DataComponents.BUNDLE_CONTENTS);
    if (bundleContents.isPresent()) {
      stacks = Iterables.concat(stacks, bundleContents.get().items());
    }

    if (HeldItemInfo.config.mergeSimilarContainerItems()) {
      Map<Component, MergedContainerEntry> entries = new LinkedHashMap<>();

      for (ItemStack stack : stacks) {
        entries.compute(
            stack.getHoverName(),
            (name, entry) -> {
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

  private abstract static class ContainerEntry {
    public abstract Component getName();

    public abstract int getCount();
  }

  private static class ItemStackContainerEntry extends ContainerEntry {
    private final ItemStack stack;

    public ItemStackContainerEntry(ItemStack stack) {
      this.stack = stack;
    }

    @Override
    public Component getName() {
      return stack.getHoverName();
    }

    @Override
    public int getCount() {
      return stack.getCount();
    }
  }

  private static class MergedContainerEntry extends ContainerEntry {
    private final Component name;
    private int count = 0;

    private MergedContainerEntry(Component name) {
      this.name = name;
    }

    @Override
    public Component getName() {
      return name;
    }

    @Override
    public int getCount() {
      return count;
    }
  }
}
