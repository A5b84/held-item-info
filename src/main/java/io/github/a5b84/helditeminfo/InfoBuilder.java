package io.github.a5b84.helditeminfo;

import com.google.gson.JsonParseException;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.item.BannerPatternItem;
import net.minecraft.item.CommandBlockItem;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.EntityBucketItem;
import net.minecraft.item.FireworkItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SignItem;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static io.github.a5b84.helditeminfo.Mod.config;

/** Creates additional lines for an item's tooltip using {@link #buildInfo(ItemStack)} */
public final class InfoBuilder {

    private InfoBuilder() {}

    private static final Formatting COLOR = Formatting.GRAY;

    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;



    /** Creates additional lines for an item's tooltip */
    public static List<Text> buildInfo(ItemStack stack) {
        final List<Text> lines = new ArrayList<>(config.maxLines());

        if (config.showName()) {
            // Item name
            final MutableText stackName = new LiteralText("") // Prevents overwriting the name formatting
                    .append(stack.getName())
                    .formatted(stack.getRarity().formatting);
            if (stack.hasCustomName()) stackName.formatted(Formatting.ITALIC);

            lines.add(stackName);
        }

        // Item-related lines
        appendItemRelatedLines(lines, stack);

        // Tag-relates lines
        if (stack.hasTag()) {
            if (config.showEnchantments()) appendEnchantments(lines, stack);
            if (config.showContainerContent()) appendContainerContent(lines, stack);
            if (config.showLore()) appendLore(lines, stack);
            if (config.showUnbreakable()) appendUnbreakable(lines, stack);
        }

        // Done
        return lines;
    }

    /** @return true if both lists are equivalent */
    public static boolean areEqual(List<InfoLine> oldInfo, List<Text> newInfo) {
        if (oldInfo.size() != newInfo.size()) return false;

        final Iterator<InfoLine> oldIt = oldInfo.iterator();
        final Iterator<Text> newIt = newInfo.iterator();

        while (oldIt.hasNext()) { // Same length -> no need to check both .hasNext()
            if (!oldIt.next().text.equals(newIt.next())) return false;
        }

        return true;
    }

    /** Converts a list of {@link Text} to a list of {@link InfoLine} */
    public static List<InfoLine> toInfoLines(List<Text> texts) {
        final List<InfoLine> info = new ArrayList<>(texts.size());
        for (Text text : texts) info.add(new InfoLine(text));
        return info;
    }

    /** Adds the content of {@code newInfo} to {@code info} and handles truncating
     * @return {@code true} if something changed */
    private static boolean appendToInfo(List<Text> info, List<? extends Text> newInfo) {
        if (info.size() >= config.maxLines()) {
            // Already full
            return false;
        }

        if (config.maxLines() - info.size() < newInfo.size()) {
            // Some room left but not enough
            final int addedLength = config.maxLines() - info.size() - 1;
            info.addAll(newInfo.subList(0, addedLength));

            info.add(
                new TranslatableText(
                    "container.shulkerBox.more",
                    newInfo.size() - addedLength
                ).formatted(COLOR, Formatting.ITALIC)
            );

            return true;
        }

        // Enough room left
        info.addAll(newInfo);
        return !newInfo.isEmpty();
    }



    /** Adds lines depending on the item itself */
    @SuppressWarnings("UnnecessaryReturnStatement")
    private static void appendItemRelatedLines(List<Text> lines, ItemStack stack) {
        // Specific stuff
        if (config.showPotionEffects() && appendPotionEffects(lines, stack)) return;
        if (config.showCommandBlockInfo() && appendCommandBlockInfo(lines, stack)) return;
        if (config.showBeehiveContent() && appendBeehiveContent(lines, stack)) return;
        if (config.showSignText() && appendSignText(lines, stack)) return;

        // Somewhat generic stuff
        if (config.showFireworkAttributes() && appendUsualTooltip(lines, stack, FireworkItem.class)) return;
        if (config.showMusicDiscDescription() && appendUsualTooltip(lines, stack, MusicDiscItem.class)) return;
        if (config.showBookMeta() && appendUsualTooltip(lines, stack, WrittenBookItem.class)) return;
        if (config.showPatternName() && appendUsualTooltip(lines, stack, BannerPatternItem.class)) return;
        if (config.showFishInBucket() && appendUsualTooltip(lines, stack, EntityBucketItem.class)) return;
    }

    /** Adds the usual tooltip (the one in the inventory)
     * @return {@code true} if something changed */
    private static boolean appendUsualTooltip(
            List<Text> info, ItemStack stack, Class<? extends Item> clazz
    ) {
        if (info.size() >= config.maxLines()
                || !clazz.isInstance(stack.getItem())) {
            return false;
        }

        // Get and add the text
        final List<Text> tooltip = new ArrayList<>();
        stack.getItem().appendTooltip(stack, null, tooltip, TooltipContext.Default.NORMAL);
        return appendToInfo(info, tooltip);
    }

    /** Adds the number of bees inside
     * @return {@code true} if something changed */
    private static boolean appendBeehiveContent(List<Text> info, ItemStack stack) {
        if (info.size() >= config.maxLines()
                || !(stack.getItem() == Items.BEE_NEST || stack.getItem() == Items.BEEHIVE)) {
            return false;
        }

        // Get the number
        final NbtCompound blockEntityTag = stack.getSubTag("BlockEntityTag");
        if (blockEntityTag == null) return false;

        final int beeCount = blockEntityTag.getList("Bees", NbtType.COMPOUND).size();
        if (beeCount == 0) return false;

        // Add it
        info.add(
            new TranslatableText("entity.minecraft.bee")
            .append(" x" + beeCount)
            .formatted(COLOR)
        );
        return true;
    }

    /** Adds a command block's command
     * @return {@code true} if something changed */
    private static boolean appendCommandBlockInfo(List<Text> info, ItemStack stack) {
        if (info.size() >= config.maxLines() || !(stack.getItem() instanceof CommandBlockItem)) {
            return false;
        }

        // Get the command
        final NbtCompound blockEntityTag = stack.getSubTag("BlockEntityTag");
        if (blockEntityTag == null) return false;

        String command = blockEntityTag.getString("Command").trim();
        if (command.isEmpty()) return false;

        List<MutableText> lines = wrapLines(command, config.maxCommandLines(), info);
        if (lines.isEmpty()) return false;

        // Formatting
        for (final MutableText text : lines) {
            text.formatted(COLOR);
        }

        // Done
        return appendToInfo(info, lines);
    }

    /**
     * Add a container's content
     * @return {@code true} if something changed
     * @see net.minecraft.block.ShulkerBoxBlock ShulkerBoxBlock#buildTooltip
     * @see net.minecraft.inventory.Inventories#readNbt
     */
    @SuppressWarnings("UnusedReturnValue")
    private static boolean appendContainerContent(List<Text> info, ItemStack stack) {
        if (info.size() >= config.maxLines()) {
            return false;
        }

        final NbtCompound tag = stack.getSubTag("BlockEntityTag");
        if (tag == null) return false;

        final NbtList items = tag.getList("Items", NbtType.COMPOUND);

        List<Text> newInfo = new ArrayList<>(config.maxLines() - info.size());

        // Loot table (same as vanilla)
        if (tag.contains("LootTable", 8)) newInfo.add(new LiteralText("???????"));

        // Items
        if (!items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                // Get the item
                final NbtCompound itemTag = items.getCompound(i);
                if (itemTag.isEmpty()) continue;
                final ItemStack iStack = ItemStack.fromNbt(itemTag);
                if (iStack.isEmpty()) continue;

                if (info.size() + newInfo.size() < config.maxLines()) {
                    // Add it if possible
                    newInfo.add(
                        iStack.getName()
                        .shallowCopy() // shallowCopy to get a MutableText
                        .append(" x" + iStack.getCount())
                        .formatted(COLOR)
                    );
                } else {
                    // If it's full and there are still items left, add `null`
                    // instead so the "and x more" shows the right number
                    newInfo.add(null);
                }
            }
        }

        return appendToInfo(info, newInfo);
    }

    /** Add an item's enchantments
     * @return {@code true} if something changed */
    @SuppressWarnings("UnusedReturnValue")
    private static boolean appendEnchantments(List<Text> info, ItemStack stack) {
        if (info.size() >= config.maxLines() || shouldHide(stack.getTag(), 1)) {
            return false;
        }

        NbtList enchantments = (stack.getItem() == Items.ENCHANTED_BOOK)
                ? EnchantedBookItem.getEnchantmentNbt(stack)
                : stack.getEnchantments();

        if (enchantments.isEmpty()) return false;

        // Filtering
        final List<Identifier> filters = config.filteredEnchants();
        if (!filters.isEmpty()) {
            NbtList filtered = new NbtList();

            for (NbtElement tag : enchantments) {
                if (tag instanceof NbtCompound) {
                    try {
                        Identifier id = new Identifier(((NbtCompound) tag).getString("id"));
                        if (filters.contains(id) == config.showOnlyFilteredEnchants()) {
                            filtered.add(tag);
                        }
                    } catch (InvalidIdentifierException ignored) {}
                }
            }

            enchantments = filtered;
        }

        final List<Text> enchantmentTexts = new ArrayList<>(enchantments.size());
        ItemStack.appendEnchantments(enchantmentTexts, enchantments);
        return appendToInfo(info, enchantmentTexts);
    }

    /** Add a potion's effects
     * @return {@code true} if something changed */
    private static boolean appendPotionEffects(List<Text> info, ItemStack stack) {
        List<Text> potionInfo = new ArrayList<>(config.maxLines());

        if (!appendUsualTooltip(potionInfo, stack, PotionItem.class)
                && !appendUsualTooltip(potionInfo, stack, LingeringPotionItem.class)
                && !appendUsualTooltip(potionInfo, stack, TippedArrowItem.class)
        ) {
            return false; // On arrive ici = c'est aucun des 3
        }

        // Remove the 'when drank: ...' lines
        for (int i = potionInfo.size() - 1; i >= 0; i--) {
            final Text line = potionInfo.get(i);
            if (line instanceof LiteralText && line.asString().equals("")) {
                // Remove everything after the first blank line
                potionInfo = potionInfo.subList(0, i);
                break;
            }
        }

        // Done
        return appendToInfo(info, potionInfo);
    }

    /** Add a sign's text
     * @return {@code true} if something changed */
    private static boolean appendSignText(List<Text> info, ItemStack stack) {
        if (info.size() >= config.maxLines() || !(stack.getItem() instanceof SignItem)) {
            return false;
        }

        // Get the text
        final NbtCompound blockEntityTag = stack.getSubTag("BlockEntityTag");
        if (blockEntityTag == null) return false;

        final List<MutableText> lines = new ArrayList<>(4);

        // Add it
        for (int i = 0; i < 4; i++) {
            MutableText text;

            try {
                text = MutableText.Serializer.fromJson(
                    blockEntityTag.getString("Text" + (i + 1))
                );
            } catch (JsonParseException e) {
                continue;
            }

            if (text == null) continue;
            final String str = text.asString();
            if (str.isBlank()) continue;

            // Add missing lines and this one
            while (lines.size() < i) lines.add(new LiteralText(""));
            lines.add(new LiteralText(str));
        }

        // Format everything
        for (MutableText text : lines) {
            text.formatted(COLOR);
        }

        return appendToInfo(info, lines);
    }

    /** Adds the item's lore
     * @return {@code true} if something changed */
    @SuppressWarnings("UnusedReturnValue")
    private static boolean appendLore(List<Text> info, ItemStack stack) {
        if (info.size() >= config.maxLines()) {
            return false;
        }

        // Get the tag
        NbtCompound displayTag = stack.getSubTag("display");
        if (displayTag == null) return false;

        NbtList loreTag = displayTag.getList("Lore", NbtType.STRING);
        if (loreTag.isEmpty()) return false;

        // Convert it to a list of text
        List<MutableText> lore = new ArrayList<>();
        for(int i = 0; i < loreTag.size(); i++) {
            String lineString = loreTag.getString(i);
            if (!(config.removePlusNbt() && lineString.equals("\"(+NBT)\""))) {
                try {
                    MutableText line = Text.Serializer.fromJson(lineString);
                    if (line != null) {
                        List<MutableText> newLines = wrapLines(line, config.maxLoreLines() - lore.size(), info);
                        lore.addAll(newLines);
                    }
                } catch (JsonParseException e) {
                    return false;
                }
            }
        }

        for (MutableText line : lore) {
            Texts.setStyleIfAbsent(line, Style.EMPTY.withColor(COLOR));
        }

        return appendToInfo(info, lore);
    }

    /** Adds the 'Unbreakable' line
     * @return {@code true} if something changed */
    @SuppressWarnings("UnusedReturnValue")
    private static boolean appendUnbreakable(List<Text> info, ItemStack stack) {
        if (info.size() >= config.maxLines() || !stack.getItem().isDamageable()) {
            return false;
        }

        final NbtCompound tag = stack.getTag();

        if (tag == null || !tag.getBoolean("Unbreakable") || shouldHide(tag, 4)) {
            return false;
        }

        info.add(
            new TranslatableText("item.unbreakable").formatted(Formatting.BLUE)
        );

        return true;
    }


    /**
     * @param maxLines The max number of lines the string will be wrapped into
     * @see ChatMessages#breakRenderedChatMessageLines
     * @see TextHandler#wrapLines(String, int, Style) */
    private static List<MutableText> wrapLines(String s, int maxLines, List<Text> info) {
        // Shorten the string
        maxLines = Math.min(config.maxLines() - info.size(), maxLines);
        if (maxLines <= 0) return Collections.emptyList();

        double maxLength = 1.25 * maxLines * config.maxLineLength(); // (*1.25 to avoid truncating too much)
        if (maxLength <= 0) return Collections.emptyList();
        if (maxLength > Integer.MAX_VALUE) maxLength = Integer.MAX_VALUE;
        //      ^ In case the user messed with their config (could crash)

        final boolean shouldCut = s.length() > maxLength;
        if (shouldCut) s = s.substring(0, (int) maxLength);

        // Split it
        final List<MutableText> fLines = new ArrayList<>(maxLines);
        final String finalString = s; // `final` required to reference it in the lambda
        double maxWidth = config.maxLineLength() * 6; // ~6px per character
        if (maxWidth > Integer.MAX_VALUE) maxWidth = Integer.MAX_VALUE;
        //      ^ In case the user messed with their config (could crash)
        TEXT_RENDERER.getTextHandler().wrapLines(
                s, (int) maxWidth, Style.EMPTY, false,
                (style, start, end) -> fLines.add(new LiteralText(finalString.substring(start, end)))
        );

        // Truncate again
        List<MutableText> lines = fLines;
        if (shouldCut || lines.size() > maxLines) {
            if (lines.size() > maxLines) lines = lines.subList(0, maxLines);
            lines.get(maxLines - 1).append("..."); // maxLines > 0
        }

        // Done
        return lines;
    }

    /**
     * @param maxLines The max number of lines the string will be wrapped into
     * @see ChatMessages#breakRenderedChatMessageLines
     * @see TextHandler#wrapLines(String, int, Style) */
    private static List<MutableText> wrapLines(StringVisitable s, int maxLines, List<Text> info) {
        // TODO somehow make this not yeet styles
        maxLines = Math.min(config.maxLines() - info.size(), maxLines);
        if (maxLines <= 0) return Collections.emptyList();

        // Split
        double maxWidth = config.maxLineLength() * 6; // ~6px per character
        if (maxWidth > Integer.MAX_VALUE) maxWidth = Integer.MAX_VALUE;
        //      ^ In case the user messed with their config (could crash)
        List<StringVisitable> strings = TEXT_RENDERER.getTextHandler().wrapLines(
                s, (int) maxWidth, Style.EMPTY
        );

        // Convert and truncate
        List<MutableText> lines = new ArrayList<>(maxLines);
        for (StringVisitable visitable : strings) {
            lines.add(new LiteralText(visitable.getString()));
            if (lines.size() >= maxLines) {
                if (strings.size() > maxLines) {
                    lines.get(maxLines - 1).append("...");
                }
                break;
            }
        }

        // Done
        return lines;
    }

    /** @return {@code true} if something should be hidden according to the
     * {@code HideFlags} tag and the user preference */
    private static boolean shouldHide(NbtCompound tag, int flag) {
        return config.respectHideFlags() && (tag.getInt("HideFlags") & flag) != 0;
    }



    /** Stores the data of one line */
    public static class InfoLine {

        public final Text text;
        public final int width;

        InfoLine(Text text) {
            this.text = text;
            width = TEXT_RENDERER.getWidth(text);
        }
    }

}
