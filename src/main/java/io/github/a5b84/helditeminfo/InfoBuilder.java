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
import net.minecraft.item.FireworkItem;
import net.minecraft.item.FishBucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SignItem;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Creates additional lines for an item's tooltip using {@link #buildInfo(ItemStack)} */
public final class InfoBuilder {

    private InfoBuilder() {}



    /** Max number of lines including the name */
    public static final int MAX_LINES = 6;

    /** Max number of lines for the command of command blocks */
    public static final int MAX_COMMAND_LINES = 2;

    /** Max length of each line of a command */
    public static final int MAX_COMMAND_LINE_LENGTH = 32;
    
    private static final Formatting COLOR = Formatting.GRAY;



    private static final TextRenderer TEXT_RENDERER = MinecraftClient.getInstance().textRenderer;



    /** Creates additional lines for an item's tooltip */
    public static List<Text> buildInfo(ItemStack stack) {
        // Item name
        final MutableText stackName = new LiteralText("") // Prevents overwriting the name formatting
                .append(stack.getName())
                .formatted(stack.getRarity().formatting);
        if (stack.hasCustomName()) stackName.formatted(Formatting.ITALIC);

        final List<Text> lines = new ArrayList<>(MAX_LINES);
        lines.add(stackName);

        // Item-related lines
        appendItemRelatedLines(lines, stack);

        // Tag-relates lines
        if (stack.hasTag()) {
            appendContainerContent(lines, stack);
            appendEnchantments(lines, stack);
            appendUnbreakable(lines, stack);
        }

        // Done
        return lines;
    }

    /** Adds lines dependant on the item itself */
    private static void appendItemRelatedLines(List<Text> lines, ItemStack stack) {
        // Specific stuff
        if (appendBeehiveContent(lines, stack)) return;
        if (appendCommandBlockInfo(lines, stack)) return;
        if (appendPotionEffects(lines, stack)) return;
        if (appendSignText(lines, stack)) return;

        // Somewhat generic stuff
        if (appendUsualTooltip(lines, stack, BannerPatternItem.class)) return;
        if (appendUsualTooltip(lines, stack, FireworkItem.class)) return;
        if (appendUsualTooltip(lines, stack, FishBucketItem.class)) return;
        if (appendUsualTooltip(lines, stack, MusicDiscItem.class)) return;
        appendUsualTooltip(lines, stack, WrittenBookItem.class);
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
        if (info.size() >= MAX_LINES) {
            // Already full
            return false;
        }

        if (MAX_LINES - info.size() < newInfo.size()) {
            // Some room left but not enough
            final int addedLength = MAX_LINES - info.size() - 1;
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



    /** Adds the usual tooltip (the one in the inventory)
     * @return {@code true} if something changed */
    private static boolean appendUsualTooltip(
            List<Text> info, ItemStack stack, Class<? extends Item> clazz
    ) {
        if (info.size() >= MAX_LINES
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
        if (info.size() >= MAX_LINES
                || !(stack.getItem() == Items.BEE_NEST || stack.getItem() == Items.BEEHIVE)) {
            return false;
        }

        // Get the number
        final CompoundTag blockEntityTag = stack.getSubTag("BlockEntityTag");
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
     * @return {@code true} if something changed
     * @see ChatMessages#breakRenderedChatMessageLines
     * @see TextHandler#wrapLines(String, int, Style)
     */
    private static boolean appendCommandBlockInfo(List<Text> info, ItemStack stack) {
        if (info.size() >= MAX_LINES || !(stack.getItem() instanceof CommandBlockItem)) {
            return false;
        }

        // Get the command
        final CompoundTag blockEntityTag = stack.getSubTag("BlockEntityTag");
        if (blockEntityTag == null) return false;

        String command = blockEntityTag.getString("Command").trim();
        if (command.isEmpty()) return false;

        // Shorten it
        final int cmdLines = Math.min(MAX_LINES - info.size(), MAX_COMMAND_LINES);
        final int maxLength = (int) (cmdLines * MAX_COMMAND_LINE_LENGTH * 1.25); // (*1.25 to leave some room)
        final boolean shouldCut = command.length() > maxLength;
        if (shouldCut) command = command.substring(0, maxLength);

        // Split it
        final List<MutableText> fLines = new ArrayList<>(cmdLines);
        final String fCommand = command; // `final` to reference it in the lambda
        TEXT_RENDERER.getTextHandler().wrapLines(
            command,
            MAX_COMMAND_LINE_LENGTH * 6, // Largeur
            Style.EMPTY, false,
            (style, start, end) -> fLines.add(new LiteralText(fCommand.substring(start, end)))
        );

        // Truncate again
        List<MutableText> lines = fLines;
        if (shouldCut || lines.size() > cmdLines) {
            if (lines.size() > cmdLines) lines = lines.subList(0, cmdLines);
            lines.get(cmdLines - 1).append("...");
        }

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
     * @see net.minecraft.inventory.Inventories#fromTag
     */
    @SuppressWarnings("UnusedReturnValue")
    private static boolean appendContainerContent(List<Text> info, ItemStack stack) {
        if (info.size() >= MAX_LINES) {
            return false;
        }

        final CompoundTag tag = stack.getSubTag("BlockEntityTag");
        if (tag == null) return false;

        final ListTag items = tag.getList("Items", NbtType.COMPOUND);

        List<Text> newInfo = new ArrayList<>(MAX_LINES - info.size());

        // Loot table (same as vanilla)
        if (tag.contains("LootTable", 8)) newInfo.add(new LiteralText("???????"));

        // Items
        if (!items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                // Get the item
                final CompoundTag itemTag = items.getCompound(i);
                if (itemTag.isEmpty()) continue;
                final ItemStack iStack = ItemStack.fromTag(itemTag);
                if (iStack.isEmpty()) continue;

                if (info.size() + newInfo.size() < MAX_LINES) {
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
        //noinspection ConstantConditions
        if (info.size() >= MAX_LINES
                || !(stack.getItem().isEnchantable(stack) || stack.getItem() instanceof EnchantedBookItem)
                || (stack.getTag().getInt("HideFlags") & 1) != 0) {
            return false;
        }

        final ListTag enchantments = (stack.getItem() == Items.ENCHANTED_BOOK)
                ? EnchantedBookItem.getEnchantmentTag(stack)
                : stack.getEnchantments();
        final List<Text> enchantmentTexts = new ArrayList<>(enchantments.size());
        ItemStack.appendEnchantments(enchantmentTexts, enchantments);
        return appendToInfo(info, enchantmentTexts);
    }

    /** Add a potion's effects
     * @return {@code true} if something changed */
    private static boolean appendPotionEffects(List<Text> info, ItemStack stack) {
        List<Text> potionInfo = new ArrayList<>(MAX_LINES);

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
        if (info.size() >= MAX_LINES || !(stack.getItem() instanceof SignItem)) {
            return false;
        }

        // Get the text
        final CompoundTag blockEntityTag = stack.getSubTag("BlockEntityTag");
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
            if (str.trim().isEmpty()) continue;

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

    /** Adds the 'Unbreakable' line
     * @return {@code true} if something changed */
    @SuppressWarnings("UnusedReturnValue")
    private static boolean appendUnbreakable(List<Text> info, ItemStack stack) {
        if (info.size() >= MAX_LINES || !stack.getItem().isDamageable()) {
            return false;
        }

        final CompoundTag tag = stack.getTag();

        if (tag == null || !tag.getBoolean("Unbreakable") || (tag.getInt("HideFlags") & 4) != 0) {
            return false;
        }

        info.add(
            new TranslatableText("item.unbreakable").formatted(Formatting.BLUE)
        );

        return true;
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
