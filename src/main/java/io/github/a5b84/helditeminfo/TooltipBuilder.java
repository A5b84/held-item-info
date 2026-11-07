package io.github.a5b84.helditeminfo;

import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.StringSplitter;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;

public class TooltipBuilder {

  public static final ChatFormatting DEFAULT_COLOR = ChatFormatting.GRAY;

  private final ItemStack stack;
  private final HeldItemInfoConfig config;
  private final Item.TooltipContext tooltipContext;
  private final StringSplitter stringSplitter;
  private final boolean textFilteringEnabled;
  private final TooltipDisplay displayComponent;
  private final List<Component> lines;

  /**
   * Real number of lines including the ones that are hidden because they would exceed {@link
   * HeldItemInfoConfig#maxLines()}.
   */
  private int realSize = 0;

  public TooltipBuilder(
      ItemStack stack,
      HeldItemInfoConfig config,
      Item.TooltipContext tooltipContext,
      StringSplitter stringSplitter,
      boolean textFilteringEnabled) {
    this.stack = stack;
    this.config = config;
    this.tooltipContext = tooltipContext;
    this.stringSplitter = stringSplitter;
    this.textFilteringEnabled = textFilteringEnabled;
    this.displayComponent =
        config.respectHideFlags()
            ? stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT)
            : TooltipDisplay.DEFAULT;
    lines = new ArrayList<>(config.maxLines());
  }

  public ItemStack getStack() {
    return stack;
  }

  public HeldItemInfoConfig getConfig() {
    return config;
  }

  public Item.TooltipContext getTooltipContext() {
    return tooltipContext;
  }

  public StringSplitter getStringSplitter() {
    return stringSplitter;
  }

  public boolean isTextFilteringEnabled() {
    return textFilteringEnabled;
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
  public <T> @Nullable T getComponentForDisplay(DataComponentType<T> componentType) {
    T component = getStack().get(componentType);
    if (component != null && displayComponent.shows(componentType)) {
      return component;
    } else {
      return null;
    }
  }

  public @Nullable CompoundTag getBlockEntityData() {
    TypedEntityData<BlockEntityType<?>> data =
        getComponentForDisplay(DataComponents.BLOCK_ENTITY_DATA);
    return data != null ? data.copyTagWithoutId() : null;
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
    T component = getComponentForDisplay(componentType);

    if (component != null) {
      component.addToTooltip(
          tooltipContext, textConsumer, TooltipFlag.NORMAL, getStack().getComponents());
    }
  }

  /**
   * @return {@code true} if at least one more line can be added.
   */
  public boolean canAdd() {
    return getRemainingLines() > 0;
  }

  public int getRemainingLines() {
    return config.maxLines() - lines.size();
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

    for (Component line : newLines) {
      if (canAdd()) {
        lines.add(line);
      } else {
        break;
      }
    }
  }

  public List<Component> build() {
    if (realSize > config.maxLines() && config.showHiddenLinesCount()) {
      Component moreText =
          Component.translatable("item.container.more_items", realSize - config.maxLines() + 1)
              .withStyle(DEFAULT_COLOR, ChatFormatting.ITALIC);
      lines.set(lines.size() - 1, moreText);
    }

    return lines;
  }
}
