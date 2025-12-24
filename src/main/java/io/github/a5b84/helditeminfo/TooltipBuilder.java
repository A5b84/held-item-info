package io.github.a5b84.helditeminfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.component.TypedEntityData;

public class TooltipBuilder {

  public static final ChatFormatting DEFAULT_COLOR = ChatFormatting.GRAY;

  private final Item.TooltipContext tooltipContext =
      Item.TooltipContext.of(Minecraft.getInstance().level);
  private final ItemStack stack;
  private final TooltipDisplay displayComponent;
  private final int maxSize = HeldItemInfo.config.maxLines();
  private final List<Component> lines;

  /**
   * Real number of lines including the ones that are hidden because they would exceed {@link
   * #maxSize}.
   */
  private int realSize = 0;

  public TooltipBuilder(ItemStack stack) {
    this.stack = stack;
    this.displayComponent =
        HeldItemInfo.config.respectHideFlags()
            ? stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT)
            : TooltipDisplay.DEFAULT;
    lines = new ArrayList<>(maxSize);
  }

  public Item.TooltipContext getTooltipContext() {
    return tooltipContext;
  }

  public ItemStack getStack() {
    return stack;
  }

  public TooltipDisplay getDisplayComponent() {
    return displayComponent;
  }

  public boolean shouldDisplayComponents() {
    return !displayComponent.hideTooltip();
  }

  /**
   * @return the requested component if it is present on the stack and should be displayed.
   */
  public <T> Optional<T> getComponentForDisplay(DataComponentType<T> componentType) {
    T component = getStack().get(componentType);
    if (component != null && displayComponent.shows(componentType)) {
      return Optional.of(component);
    } else {
      return Optional.empty();
    }
  }

  public Optional<CompoundTag> getBlockEntityData() {
    return getComponentForDisplay(DataComponents.BLOCK_ENTITY_DATA)
        .map(TypedEntityData::copyTagWithoutId);
  }

  public <T extends TooltipProvider> void appendComponent(DataComponentType<T> componentType) {
    appendComponent(componentType, (Consumer<Component>) this::append);
  }

  public <T extends TooltipProvider> void appendComponent(
      DataComponentType<T> componentType, UnaryOperator<Component> transformation) {
    appendComponent(
        componentType,
        text -> {
          append(canAdd() ? transformation.apply(text) : text);
        });
  }

  public <T extends TooltipProvider> void appendComponent(
      DataComponentType<T> componentType, Consumer<Component> textConsumer) {
    getComponentForDisplay(componentType)
        .ifPresent(
            component ->
                component.addToTooltip(
                    getTooltipContext(),
                    textConsumer,
                    TooltipFlag.NORMAL,
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

  public void append(Component text) {
    realSize++;
    if (canAdd()) {
      lines.add(text);
    }
  }

  public void append(Supplier<Component> textSupplier) {
    realSize++;
    if (canAdd()) {
      lines.add(textSupplier.get());
    }
  }

  public void appendAll(List<? extends Component> newLines) {
    realSize += newLines.size();

    if (canAdd()) {
      for (Component line : newLines) {
        lines.add(line);
        if (!canAdd()) break;
      }
    }
  }

  public List<Component> build() {
    if (realSize > maxSize && HeldItemInfo.config.showHiddenLinesCount()) {
      Component moreText =
          Component.translatable("item.container.more_items", realSize - maxSize + 1)
              .withStyle(DEFAULT_COLOR, ChatFormatting.ITALIC);
      lines.set(lines.size() - 1, moreText);
    }

    return lines;
  }
}
