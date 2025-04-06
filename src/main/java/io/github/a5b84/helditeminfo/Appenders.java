package io.github.a5b84.helditeminfo;

import io.github.a5b84.helditeminfo.mixin.ItemEnchantmentsComponentAccessor;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

public final class Appenders {

    private static final Style LORE_STYLE = Style.EMPTY.withColor(TooltipBuilder.DEFAULT_COLOR).withItalic(true);
    private static final Text UNBREAKABLE_TEXT = Text.translatable("item.unbreakable").formatted(TooltipBuilder.DEFAULT_COLOR);

    private Appenders() {}


    public static void appendPotionEffects(TooltipBuilder builder) {
        // Using the vanilla tooltip and removing the 'When applied: ...' lines (everything after the first blank line)
        AtomicBoolean shouldAddLine = new AtomicBoolean(true);
        builder.appendComponent(DataComponentTypes.POTION_CONTENTS,
                text -> {
                    if (Util.isBlank(text)) {
                        shouldAddLine.set(false);
                    } else if (shouldAddLine.get()) {
                        builder.append(text);
                    }
                });
    }


    /**
     * @see JukeboxPlayableComponent#appendTooltip
     */
    public static void appendMusicDiscDescription(TooltipBuilder builder) {
        builder.getComponentForDisplay(DataComponentTypes.JUKEBOX_PLAYABLE)
                .ifPresent(songComponent -> {
                    RegistryWrapper.WrapperLookup registryLookup = Objects.requireNonNull(builder.getTooltipContext().getRegistryLookup());
                    songComponent.song().resolveEntry(registryLookup).ifPresent(entry -> builder.append(() -> {
                        MutableText description = entry.value().description().copy();
                        return Texts.setStyleIfAbsent(description, Style.EMPTY.withColor(TooltipBuilder.DEFAULT_COLOR));
                    }));
                });
    }


    public static void appendEnchantments(TooltipBuilder builder) {
        appendEnchantments(builder, DataComponentTypes.STORED_ENCHANTMENTS);
        appendEnchantments(builder, DataComponentTypes.ENCHANTMENTS);
    }

    /**
     * @see ItemEnchantmentsComponent#appendTooltip(Item.TooltipContext, Consumer, TooltipType, ComponentsAccess)
     */
    private static void appendEnchantments(TooltipBuilder builder, ComponentType<ItemEnchantmentsComponent> componentType) {
        builder.getComponentForDisplay(componentType)
                .ifPresent(enchantments -> {
                    RegistryEntryList<Enchantment> tooltipOrder = ItemEnchantmentsComponentAccessor.callGetTooltipOrderList(builder.getTooltipContext().getRegistryLookup(), RegistryKeys.ENCHANTMENT, EnchantmentTags.TOOLTIP_ORDER);

                    for (RegistryEntry<Enchantment> enchantment : tooltipOrder) {
                        int level = enchantments.getLevel(enchantment);
                        if (level > 0 && shouldShowEnchantment(enchantment)) {
                            builder.append(() -> Enchantment.getName(enchantment, level));
                        }
                    }

                    for (var mapEntry : enchantments.getEnchantmentEntries()) {
                        RegistryEntry<Enchantment> enchantment = mapEntry.getKey();
                        if (!tooltipOrder.contains(enchantment) && shouldShowEnchantment(enchantment)) {
                            builder.append(() -> {
                                int level = mapEntry.getIntValue();
                                return Enchantment.getName(enchantment, level);
                            });
                        }
                    }
        });
    }

    private static boolean shouldShowEnchantment(RegistryEntry<Enchantment> entry) {
        List<Identifier> filters = HeldItemInfo.filteredEnchantments;
        if (filters.isEmpty()) {
            return true;
        } else {
            Identifier id = entry.getKey().map(RegistryKey::getValue).orElse(null);
            return filters.contains(id) == config.showOnlyFilteredEnchantments();
        }
    }


    public static void appendItem(TooltipBuilder builder, NbtCompound itemNbt) {
        if (itemNbt == null || itemNbt.isEmpty()) return;

        //noinspection DataFlowIssue (Argument 'builder.tooltipContext.getRegistryLookup()' might be null)
        Optional<ItemStack> optionalStack = ItemStack.fromNbt(builder.getTooltipContext().getRegistryLookup(), itemNbt);
        ItemStack stack;
        if (optionalStack.isEmpty() || (stack = optionalStack.get()).isEmpty()) return;

        builder.append(() -> Text.translatable("item.container.item_count", stack.getName(), stack.getCount())
                .formatted(TooltipBuilder.DEFAULT_COLOR));
    }


    public static void appendLore(TooltipBuilder builder) {
        builder.getComponentForDisplay(DataComponentTypes.LORE)
                .ifPresent(loreComponent -> {
                    int currentLoreLines = 0;

                    for (Text line : loreComponent.lines()) {
                        int maxLines = Math.min(config.maxLoreLines() - currentLoreLines, builder.getRemainingLines());
                        List<MutableText> wrappedLine = Util.wrapLines(line, maxLines);

                        for (MutableText linePart : wrappedLine) {
                            builder.append(() -> Texts.setStyleIfAbsent(linePart, LORE_STYLE));
                        }
                    }
                });
    }


    public static void appendUnbreakable(TooltipBuilder builder) {
        builder.getComponentForDisplay(DataComponentTypes.UNBREAKABLE)
                .ifPresent(component -> builder.append(UNBREAKABLE_TEXT));
    }
}
