package io.github.a5b84.helditeminfo;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TooltipBuilder {

  public static final Formatting DEFAULT_COLOR = Formatting.GRAY;

  private final Item.TooltipContext tooltipContext =
      Item.TooltipContext.create(MinecraftClient.getInstance().world);
  private final ItemStack stack;
  private final TooltipDisplayComponent displayComponent;
  private final int maxSize = config.maxLines();
  private final List<Text> lines;

  /**
   * Real number of lines including the ones that are hidden because they would exceed {@link
   * #maxSize}.
   */
  private int realSize = 0;

  public TooltipBuilder(ItemStack stack) {
    this.stack = stack;
    this.displayComponent =
        config.respectHideFlags()
            ? stack.getOrDefault(
                DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT)
            : TooltipDisplayComponent.DEFAULT;
    lines = new ArrayList<>(maxSize);
  }

  public Item.TooltipContext getTooltipContext() {
    return tooltipContext;
  }

  public ItemStack getStack() {
    return stack;
  }

  public TooltipDisplayComponent getDisplayComponent() {
    return displayComponent;
  }

  public boolean shouldDisplayComponents() {
    return !displayComponent.hideTooltip();
  }

  /**
   * @return the requested component if it is present on the stack and should be displayed.
   */
  public <T> Optional<T> getComponentForDisplay(ComponentType<T> componentType) {
    T component = getStack().get(componentType);
    if (component != null && displayComponent.shouldDisplay(componentType)) {
      return Optional.of(component);
    } else {
      return Optional.empty();
    }
  }

  public Optional<NbtCompound> getBlockEntityData() {
    return getComponentForDisplay(DataComponentTypes.BLOCK_ENTITY_DATA)
        .map(TypedEntityData::copyNbtWithoutId);
  }

  public <T extends TooltipAppender> void appendComponent(ComponentType<T> componentType) {
    appendComponent(componentType, (Consumer<Text>) this::append);
  }

  public <T extends TooltipAppender> void appendComponent(
      ComponentType<T> componentType, UnaryOperator<Text> transformation) {
    appendComponent(
        componentType,
        text -> {
          append(canAdd() ? transformation.apply(text) : text);
        });
  }

  public <T extends TooltipAppender> void appendComponent(
      ComponentType<T> componentType, Consumer<Text> textConsumer) {
    getComponentForDisplay(componentType)
        .ifPresent(
            component ->
                component.appendTooltip(
                    getTooltipContext(),
                    textConsumer,
                    TooltipType.BASIC,
                    getStack().getComponents()));
  }

  /**
   * @return {@code true} iff at least one more line can be added.
   */
  public boolean canAdd() {
    return lines.size() < maxSize;
  }

  public int getRemainingLines() {
    return maxSize - lines.size();
  }

  public void append(Text text) {
    realSize++;
    if (canAdd()) {
      lines.add(text);
    }
  }

  public void append(Supplier<Text> textSupplier) {
    realSize++;
    if (canAdd()) {
      lines.add(textSupplier.get());
    }
  }

  public void appendAll(List<? extends Text> newLines) {
    realSize += newLines.size();

    if (canAdd()) {
      for (Text line : newLines) {
        lines.add(line);
        if (!canAdd()) break;
      }
    }
  }

  public List<Text> build() {
    if (realSize > maxSize && config.showHiddenLinesCount()) {
      Text moreText =
          Text.translatable("item.container.more_items", realSize - maxSize + 1)
              .formatted(DEFAULT_COLOR, Formatting.ITALIC);
      lines.set(lines.size() - 1, moreText);
    }

    return lines;
  }
}
