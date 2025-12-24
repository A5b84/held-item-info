package io.github.a5b84.helditeminfo;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

public final class ContainerContentAppender {

  private ContainerContentAppender() {}

  /**
   * @see ContainerComponent#appendTooltip(Item.TooltipContext, Consumer, TooltipType,
   *     ComponentsAccess)
   */
  public static void appendContainerContent(TooltipBuilder builder) {
    builder.appendComponent(DataComponentTypes.CONTAINER_LOOT);

    for (ContainerEntry innerStack : getContainerEntries(builder)) {
      builder.append(
          () ->
              Text.translatable(
                      "item.container.item_count", innerStack.getName(), innerStack.getCount())
                  .formatted(TooltipBuilder.DEFAULT_COLOR));
    }
  }

  private static Iterable<? extends ContainerEntry> getContainerEntries(TooltipBuilder builder) {
    Iterable<ItemStack> stacks = Collections.emptyList();

    Optional<ContainerComponent> containerComponent =
        builder.getComponentForDisplay(DataComponentTypes.CONTAINER);
    if (containerComponent.isPresent()) {
      stacks = containerComponent.get().iterateNonEmpty();
    }

    Optional<BundleContentsComponent> bundleContents =
        builder.getComponentForDisplay(DataComponentTypes.BUNDLE_CONTENTS);
    if (bundleContents.isPresent()) {
      stacks = Iterables.concat(stacks, bundleContents.get().iterate());
    }

    if (HeldItemInfo.config.mergeSimilarContainerItems()) {
      Map<Text, MergedContainerEntry> entries = new LinkedHashMap<>();

      for (ItemStack stack : stacks) {
        entries.compute(
            stack.getName(),
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
