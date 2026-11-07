package io.github.a5b84.helditeminfo;

import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig;
import io.github.a5b84.helditeminfo.mixin.ItemEnchantmentsAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
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
        HeldItemInfo.getIntangibleProjectileVisibility().pushHidden();
        builder.appendComponent(DataComponents.CHARGED_PROJECTILES, Util::withDefaultColor);
        HeldItemInfo.getIntangibleProjectileVisibility().popHidden();
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
        appendEnchantments(builder);
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

  private static void appendEnchantments(TooltipBuilder builder) {
    appendEnchantments(builder, DataComponents.STORED_ENCHANTMENTS);
    appendEnchantments(builder, DataComponents.ENCHANTMENTS);
  }

  /**
   * @see ItemEnchantments#addToTooltip(Item.TooltipContext, Consumer, TooltipFlag,
   *     DataComponentGetter)
   */
  private static void appendEnchantments(
      TooltipBuilder builder, DataComponentType<ItemEnchantments> componentType) {
    ItemEnchantments enchantments = builder.getComponentForDisplay(componentType);

    if (enchantments != null) {
      HolderSet<Enchantment> tooltipOrder =
          ItemEnchantmentsAccessor.callGetTagOrEmpty(
              builder.getTooltipContext().registries(),
              Registries.ENCHANTMENT,
              EnchantmentTags.TOOLTIP_ORDER);
      HeldItemInfoConfig config = builder.getConfig();

      for (Holder<Enchantment> enchantment : tooltipOrder) {
        int level = enchantments.getLevel(enchantment);
        if (level > 0 && shouldShowEnchantment(enchantment, config)) {
          builder.append(() -> Enchantment.getFullname(enchantment, level));
        }
      }

      for (var mapEntry : enchantments.entrySet()) {
        Holder<Enchantment> enchantment = mapEntry.getKey();
        if (!tooltipOrder.contains(enchantment) && shouldShowEnchantment(enchantment, config)) {
          builder.append(() -> {
            int level = mapEntry.getIntValue();
            return Enchantment.getFullname(enchantment, level);
          });
        }
      }
    }
  }

  private static boolean shouldShowEnchantment(
      Holder<Enchantment> entry, HeldItemInfoConfig config) {
    Set<Identifier> filters = config.filteredEnchantments();

    if (filters.isEmpty()) {
      return true;
    } else {
      Identifier id = entry.unwrapKey().map(ResourceKey::identifier).orElse(null);
      return filters.contains(id) == config.showOnlyFilteredEnchantments();
    }
  }

  public static void appendStack(TooltipBuilder builder, ItemStack stack) {
    if (!stack.isEmpty()) {
      builder.append(() ->
          Component.translatable(
                  "item.container.item_count", stack.getHoverName(), stack.getCount())
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
