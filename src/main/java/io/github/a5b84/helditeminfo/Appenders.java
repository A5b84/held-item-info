package io.github.a5b84.helditeminfo;

import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import java.util.ArrayList;
import java.util.List;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;
import static io.github.a5b84.helditeminfo.Util.hasHideFlag;

public final class Appenders {

    private Appenders() {}

    public static void appendEnchantments(TooltipBuilder builder) {
        // Get the enchantments
        ItemStack stack = builder.stack;
        if (hasHideFlag(stack.getNbt(), 1)) return;

        NbtList enchantments = (stack.getItem() == Items.ENCHANTED_BOOK)
                ? EnchantedBookItem.getEnchantmentNbt(stack)
                : stack.getEnchantments();

        if (enchantments.isEmpty()) return;

        // Filtering
        List<Identifier> filters = HeldItemInfo.filteredEnchantments;
        if (!filters.isEmpty()) {
            NbtList filtered = new NbtList();

            for (NbtElement tag : enchantments) {
                if (tag instanceof NbtCompound) {
                    try {
                        Identifier id = new Identifier(((NbtCompound) tag).getString("id"));
                        if (filters.contains(id) == config.showOnlyFilteredEnchantments()) {
                            filtered.add(tag);
                        }
                    } catch (InvalidIdentifierException ignored) {}
                }
            }

            enchantments = filtered;
        }

        List<Text> enchantmentTexts = new ArrayList<>(enchantments.size());
        ItemStack.appendEnchantments(enchantmentTexts, enchantments);
        builder.appendAll(enchantmentTexts);
    }

    public static void appendContainerContent(TooltipBuilder builder) {
        @SuppressWarnings("unused")
        // Shulker Boxes, Chests, ...
        boolean added = appendContainerContent(builder, builder.stack.getNbt())
                // Bundles
                || appendContainerContent(builder, builder.stack.getSubNbt("BlockEntityTag"));
    }

    /**
     * Add the items in {@code tag} to the tooltip.
     * @return {@code true} if something was added.
     * @see net.minecraft.block.ShulkerBoxBlock#appendTooltip
     * @see net.minecraft.inventory.Inventories#readNbt
     */
    private static boolean appendContainerContent(TooltipBuilder builder, NbtCompound tag) {
        if (tag == null) return false;

        NbtList items = tag.getList("Items", NbtType.COMPOUND);

        if (tag.contains("LootTable", NbtType.STRING)) {
            // Loot table (same as vanilla shulker boxes)
            builder.append(Text.literal("???????"));
            return true;

        } else if (!items.isEmpty()) {
            boolean added = false;

            for (NbtElement itemElement : items) {
                if (!(itemElement instanceof NbtCompound itemTag)
                        || itemTag.isEmpty()) {
                    continue;
                }
                ItemStack iStack = ItemStack.fromNbt(itemTag);
                if (iStack.isEmpty()) continue;

                Text text;
                if (builder.canAdd()) {
                    text = iStack.getName()
                            .copy() // shallowCopy to get a MutableText
                            .append(" x" + iStack.getCount())
                            .formatted(TooltipBuilder.DEFAULT_COLOR);
                } else {
                    // If it's full and there are still items left, add `null`
                    // instead so the "and x more" shows the right number
                    text = null;
                }

                builder.append(text);
                added = true;
            }

            return added;
        } else {
            return false;
        }
    }


    /**
     * Adds the item's lore to the tooltip.
     */
    public static void appendLore(TooltipBuilder builder) {
        // Get the tag
        NbtCompound displayTag = builder.stack.getSubNbt("display");
        if (displayTag == null) return;

        NbtList loreTag = displayTag.getList("Lore", NbtType.STRING);
        if (loreTag.isEmpty()) return;

        // Convert it to a list of text
        List<MutableText> lore = new ArrayList<>();
        for(int i = 0; i < loreTag.size(); i++) {
            String lineString = loreTag.getString(i);
            if (!(config.removePlusNbt() && lineString.equals("\"(+NBT)\""))) {
                try {
                    MutableText line = Text.Serializer.fromJson(lineString);
                    if (line != null) {
                        int maxLines = Math.min(config.maxLoreLines() - lore.size(), builder.getRemainingLines());
                        List<MutableText> newLines = Util.wrapLines(line, maxLines);
                        lore.addAll(newLines);
                    }
                } catch (JsonParseException e) {
                    return;
                }
            }
        }

        for (MutableText line : lore) {
            Texts.setStyleIfAbsent(line, Style.EMPTY.withColor(TooltipBuilder.DEFAULT_COLOR));
        }

        builder.appendAll(lore);
    }


    /**
     * Adds a line if the item has the unbreakable modifier.
     */
    public static void appendUnbreakable(TooltipBuilder builder) {
        if (!builder.stack.getItem().isDamageable()) {
            return;
        }

        NbtCompound tag = builder.stack.getNbt();
        if (tag == null
                || !tag.getBoolean("Unbreakable")
                || Util.hasHideFlag(tag, 4)) {
            return;
        }

        builder.append(Text.translatable("item.unbreakable").formatted(Formatting.BLUE));
    }

}
