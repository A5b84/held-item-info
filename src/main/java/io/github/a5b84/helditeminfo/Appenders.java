package io.github.a5b84.helditeminfo;

import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.entity.SignText;

public final class Appenders {

  private static final Style LORE_STYLE =
      Style.EMPTY.withColor(TooltipBuilder.DEFAULT_COLOR).withItalic(true);
  private static final Component UNBREAKABLE_TEXT =
      Component.translatable("item.unbreakable").withStyle(TooltipBuilder.DEFAULT_COLOR);
  private static final MutableComponent SIGN_SIDE_SEPARATOR =
      Component.translatable("held_item_info.tooltip.sign_side_separator")
          .withStyle(TooltipBuilder.DEFAULT_COLOR);

  private Appenders() {}

  public static void appendAll(TooltipBuilder builder) {
    ItemStack stack = builder.getStack();
    HeldItemInfoConfig config = builder.getConfig();
    HeldItemTooltipContext context =
        HeldItemTooltipContext.instance = new HeldItemTooltipContext(config);

    if (config.showName()) {
      appendStackName(stack, builder);
    }

    if (builder.shouldDisplayComponents()) {
      Item item = stack.getItem();
      if (item instanceof TooltipAppender appender
          && appender.heldItemInfo_shouldAppendTooltip(config)) {
        appender.heldItemInfo_appendTooltip(builder);
      }

      if (item instanceof BlockItem blockItem
          && blockItem.getBlock() instanceof TooltipAppender appender
          && appender.heldItemInfo_shouldAppendTooltip(config)) {
        appender.heldItemInfo_appendTooltip(builder);
      }

      if (config.showEntityBucketContent()) {
        builder.appendComponent(DataComponents.TROPICAL_FISH_PATTERN);
      }

      if (config.showGoatHornInstrument()) {
        builder.appendComponent(DataComponents.INSTRUMENT);
      }

      if (config.showFilledMapId()) {
        builder.appendComponent(DataComponents.MAP_ID);
      }

      if (config.showBeehiveContent()) {
        builder.appendComponent(DataComponents.BEES);
      }

      if (config.showContainerContent()) {
        ContainerContentAppender.appendContainerContent(builder);
      }

      if (config.showBookMeta()) {
        builder.appendComponent(DataComponents.WRITTEN_BOOK_CONTENT);
      }

      if (config.showCrossbowProjectiles()) {
        context.hideIntangibleTooltip = true;
        builder.appendComponent(DataComponents.CHARGED_PROJECTILES, Util::withDefaultColor);
        context.hideIntangibleTooltip = false;
      }

      if (config.showFireworkAttributes()) {
        builder.appendComponent(DataComponents.FIREWORKS);
      }

      if (config.showFireworkAttributes()) {
        builder.appendComponent(DataComponents.FIREWORK_EXPLOSION);
      }

      if (config.showPotionEffects()) {
        appendPotionEffects(builder);
      }

      if (config.showMusicDiscDescription()) {
        appendMusicDiscDescription(builder);
      }

      if (config.showEnchantments()) {
        context.filterEnchantments = true;
        builder.appendComponent(DataComponents.ENCHANTMENTS);
        context.filterEnchantments = false;
        builder.appendComponent(DataComponents.STORED_ENCHANTMENTS);
      }

      if (config.showLore()) {
        appendLore(builder);
      }

      if (config.showUnbreakable()) {
        appendUnbreakable(builder);
      }

      if (config.showSignText()) {
        appendSignText(builder);
      }

      if (config.showPotionEffects()) {
        builder.appendComponent(DataComponents.OMINOUS_BOTTLE_AMPLIFIER);
      }

      if (config.showBlockState()) {
        builder.appendComponent(DataComponents.BLOCK_STATE);
      }
    }

    HeldItemTooltipContext.instance = null;
  }

  private static void appendStackName(ItemStack stack, TooltipBuilder builder) {
    // Using Component.empty().append(...).withStyle(...) because withStyle(...) mutates the object
    // (see ItemStack.getStyledHoverName)
    MutableComponent stackName =
        Component.empty().append(stack.getHoverName()).withStyle(stack.getRarity().color());
    if (stack.has(DataComponents.CUSTOM_NAME)) {
      stackName.withStyle(ChatFormatting.ITALIC);
    }

    builder.append(stackName);
  }

  private static void appendPotionEffects(TooltipBuilder builder) {
    // Using the vanilla tooltip and removing the 'When applied: ...' lines (everything after the
    // first blank line)
    AtomicBoolean shouldAddLine = new AtomicBoolean(true);
    builder.appendComponent(
        DataComponents.POTION_CONTENTS,
        text -> {
          if (Util.isBlank(text)) {
            shouldAddLine.set(false);
          } else if (shouldAddLine.get()) {
            builder.append(text);
          }
        });
  }

  /**
   * @see JukeboxPlayable#addToTooltip
   */
  private static void appendMusicDiscDescription(TooltipBuilder builder) {
    JukeboxPlayable songComponent = builder.getComponentForDisplay(DataComponents.JUKEBOX_PLAYABLE);

    if (songComponent != null) {
      builder.append(() ->
          ComponentUtils.mergeStyles(
              songComponent.song().value().description(),
              Style.EMPTY.withColor(TooltipBuilder.DEFAULT_COLOR)));
    }
  }

  public static void appendStack(TooltipBuilder builder, ItemStack stack) {
    if (!stack.isEmpty()) {
      builder.append(() ->
          Component.translatable(
                  "item.container.item_count", stack.getCount(), stack.getHoverName())
              .withStyle(TooltipBuilder.DEFAULT_COLOR));
    }
  }

  private static void appendLore(TooltipBuilder builder) {
    ItemLore loreComponent = builder.getComponentForDisplay(DataComponents.LORE);

    if (loreComponent != null) {
      HeldItemInfoConfig config = builder.getConfig();
      int maxRemainingLoreLines = config.maxLoreLines();

      for (Component line : loreComponent.lines()) {
        int maxLines = Math.min(maxRemainingLoreLines, builder.getRemainingLines());
        List<MutableComponent> wrappedLine =
            Util.wrapLines(line, builder.getStringSplitter(), maxLines, config);
        maxRemainingLoreLines -= wrappedLine.size();

        for (MutableComponent linePart : wrappedLine) {
          builder.append(() -> ComponentUtils.mergeStyles(linePart, LORE_STYLE));
        }
      }
    }
  }

  private static void appendUnbreakable(TooltipBuilder builder) {
    if (builder.getComponentForDisplay(DataComponents.UNBREAKABLE) != null) {
      builder.append(UNBREAKABLE_TEXT);
    }
  }

  private static void appendSignText(TooltipBuilder builder) {
    List<MutableComponent> frontLines = getSignSideText(builder, DataComponents.SIGN_TEXT_FRONT);
    List<MutableComponent> backLines = getSignSideText(builder, DataComponents.SIGN_TEXT_BACK);

    if (!frontLines.isEmpty()) {
      builder.appendAll(frontLines);
    }

    if (!backLines.isEmpty()) {
      if (!frontLines.isEmpty()) {
        builder.append(SIGN_SIDE_SEPARATOR);
      }

      builder.appendAll(backLines);
    }
  }

  private static List<MutableComponent> getSignSideText(
      TooltipBuilder builder, DataComponentType<SignText> componentType) {
    SignText signText = builder.getComponentForDisplay(componentType);

    if (signText != null) {
      List<Component> messages = signText.getMessages(builder.isTextFilteringEnabled());
      List<MutableComponent> lines = new ArrayList<>(messages.size());

      for (Component message : messages) {
        String messageStr = message.getString();
        if (!messageStr.isBlank()) {
          lines.add(Component.literal(messageStr).withStyle(TooltipBuilder.DEFAULT_COLOR));
        }
      }

      return lines;
    } else {
      return Collections.emptyList();
    }
  }
}
