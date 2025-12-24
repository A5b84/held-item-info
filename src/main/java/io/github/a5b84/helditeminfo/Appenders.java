package io.github.a5b84.helditeminfo;

import io.github.a5b84.helditeminfo.mixin.ItemEnchantmentsAccessor;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class Appenders {

  private static final Style LORE_STYLE =
      Style.EMPTY.withColor(TooltipBuilder.DEFAULT_COLOR).withItalic(true);
  private static final Component UNBREAKABLE_TEXT =
      Component.translatable("item.unbreakable").withStyle(TooltipBuilder.DEFAULT_COLOR);

  private Appenders() {}

  public static void appendPotionEffects(TooltipBuilder builder) {
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
  public static void appendMusicDiscDescription(TooltipBuilder builder) {
    builder
        .getComponentForDisplay(DataComponents.JUKEBOX_PLAYABLE)
        .ifPresent(
            songComponent -> {
              HolderLookup.Provider registryLookup =
                  Objects.requireNonNull(builder.getTooltipContext().registries());
              songComponent
                  .song()
                  .unwrap(registryLookup)
                  .ifPresent(
                      entry ->
                          builder.append(
                              () -> {
                                MutableComponent description = entry.value().description().copy();
                                return ComponentUtils.mergeStyles(
                                    description,
                                    Style.EMPTY.withColor(TooltipBuilder.DEFAULT_COLOR));
                              }));
            });
  }

  public static void appendEnchantments(TooltipBuilder builder) {
    appendEnchantments(builder, DataComponents.STORED_ENCHANTMENTS);
    appendEnchantments(builder, DataComponents.ENCHANTMENTS);
  }

  /**
   * @see ItemEnchantments#addToTooltip(Item.TooltipContext, Consumer, TooltipFlag,
   *     DataComponentGetter)
   */
  private static void appendEnchantments(
      TooltipBuilder builder, DataComponentType<ItemEnchantments> componentType) {
    builder
        .getComponentForDisplay(componentType)
        .ifPresent(
            enchantments -> {
              HolderSet<Enchantment> tooltipOrder =
                  ItemEnchantmentsAccessor.callGetTagOrEmpty(
                      builder.getTooltipContext().registries(),
                      Registries.ENCHANTMENT,
                      EnchantmentTags.TOOLTIP_ORDER);

              for (Holder<Enchantment> enchantment : tooltipOrder) {
                int level = enchantments.getLevel(enchantment);
                if (level > 0 && shouldShowEnchantment(enchantment)) {
                  builder.append(() -> Enchantment.getFullname(enchantment, level));
                }
              }

              for (var mapEntry : enchantments.entrySet()) {
                Holder<Enchantment> enchantment = mapEntry.getKey();
                if (!tooltipOrder.contains(enchantment) && shouldShowEnchantment(enchantment)) {
                  builder.append(
                      () -> {
                        int level = mapEntry.getIntValue();
                        return Enchantment.getFullname(enchantment, level);
                      });
                }
              }
            });
  }

  private static boolean shouldShowEnchantment(Holder<Enchantment> entry) {
    List<ResourceLocation> filters = HeldItemInfo.filteredEnchantments;
    if (filters.isEmpty()) {
      return true;
    } else {
      ResourceLocation id = entry.unwrapKey().map(ResourceKey::location).orElse(null);
      return filters.contains(id) == HeldItemInfo.config.showOnlyFilteredEnchantments();
    }
  }

  public static void appendStack(TooltipBuilder builder, ItemStack stack) {
    if (!stack.isEmpty()) {
      builder.append(
          () ->
              Component.translatable(
                      "item.container.item_count", stack.getHoverName(), stack.getCount())
                  .withStyle(TooltipBuilder.DEFAULT_COLOR));
    }
  }

  public static void appendLore(TooltipBuilder builder) {
    builder
        .getComponentForDisplay(DataComponents.LORE)
        .ifPresent(
            loreComponent -> {
              int currentLoreLines = 0;

              for (Component line : loreComponent.lines()) {
                int maxLines =
                    Math.min(
                        HeldItemInfo.config.maxLoreLines() - currentLoreLines,
                        builder.getRemainingLines());
                List<MutableComponent> wrappedLine = Util.wrapLines(line, maxLines);

                for (MutableComponent linePart : wrappedLine) {
                  builder.append(() -> ComponentUtils.mergeStyles(linePart, LORE_STYLE));
                }
              }
            });
  }

  public static void appendUnbreakable(TooltipBuilder builder) {
    builder
        .getComponentForDisplay(DataComponents.UNBREAKABLE)
        .ifPresent(component -> builder.append(UNBREAKABLE_TEXT));
  }
}
