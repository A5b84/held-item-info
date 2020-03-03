package io.github.a5b84.helditeminfo;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonParseException;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.util.Texts;
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
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

/**
 * Permet de créer la liste d'infos d'un item (liste de textes affichables)
 * avec {@link #buildInfo(ItemStack)}.
 */
public final class ItemInfo {

    private ItemInfo() {}



    /** Nombre max de lignes, nom inclu */
    public static final int MAX_LINES = 6;

    /** Nombre max de lignes pour une commande */
    public static final int MAX_COMMAND_LINES = 2;

    /** ~Taille maximale de chaque ligne de commande en caractères */
    public static final int MAX_COMMAND_LINE_LENGTH = 32;



    protected static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;



    /** Crée la liste d'infos de l'item (nom inclu) */
    public static List<InfoLine> buildInfo(final ItemStack stack) {
        // Nom de l'item
        final Text stackName = new LiteralText("") // Pour pas remplacer les styles
                .append(stack.getName())
                .formatted(stack.getRarity().formatting);
        if (stack.hasCustomName()) stackName.formatted(Formatting.ITALIC);

        final List<Text> texts = new ArrayList<>(MAX_LINES);
        texts.add(stackName);

        do {
            // Trucs qui dépendent de l'item
            do {
                // Trucs spécifiques
                if (appendBeehiveContent(texts, stack)) break;
                if (appendCommandBlockInfo(texts, stack)) break;
                if (appendPotionEffects(texts, stack)) break;
                if (appendSignText(texts, stack)) break;

                // Trucs génériques
                if (appendUsualTooltip(texts, stack, BannerPatternItem.class)) break;
                if (appendUsualTooltip(texts, stack, FireworkItem.class)) break;
                if (appendUsualTooltip(texts, stack, FishBucketItem.class)) break;
                if (appendUsualTooltip(texts, stack, MusicDiscItem.class)) break;
                if (appendUsualTooltip(texts, stack, WrittenBookItem.class)) break;

            } while (false);

            // Trucs qui dépendent du tag
            if (!stack.hasTag()) break;

            appendContainerContent(texts, stack);
            appendEnchantments(texts, stack);
            appendUnbreakable(texts, stack);
        } while (false);

        // Presque fini
        final List<InfoLine> info = new ArrayList<>(texts.size());
        for (Text text : texts) info.add(new InfoLine(text));

        return info;
    }



    /**
     * Ajoute une liste de trucs aux infos et le recoupe si nécessaire.
     * @param info Infos
     * @param list Trucs à ajouter
     * @return `true` si un truc a été ajouté
     */
    protected static boolean appendToInfo(List<Text> info, List<Text> list) {
        if (info.size() >= MAX_LINES) {
            // Cas infos déjà pleines
            return false;
        }

        if (MAX_LINES - info.size() < list.size()) {
            // Cas infos pas encore pleines mais qui déborderont
            final int addedLength = MAX_LINES - info.size() - 1;
            info.addAll(list.subList(0, addedLength));

            info.add(
                new TranslatableText(
                    "container.shulkerBox.more",
                    list.size() - addedLength
                ).formatted(Formatting.GRAY, Formatting.ITALIC)
            );

            return true;
        }

        // Cas assez de place (dont liste vide)
        info.addAll(list);
        return !info.isEmpty();
    }



    /** Ajoute le tooltip habituel (de dans l'inventaire) aux infos.
     * @return `true` si un truc a été ajouté */
    protected static boolean appendUsualTooltip(
            List<Text> info,
            ItemStack stack,
            Class<? extends Item> clazz
    ) {
        if (info.size() >= MAX_LINES
                || !clazz.isInstance(stack.getItem())) {
            return false;
        }

        // On récupère le texte et on l'ajoute
        final List<Text> tooltip = new ArrayList<>();
        stack.getItem().appendTooltip(stack, null, tooltip, TooltipContext.Default.NORMAL);
        return appendToInfo(info, tooltip);
    }

    /**
     * Ajoute la commande d'un commande block aux infos.
     * @param info Infos
     * @param stack Stack de l'item (avec un tag)
     * @return `true` si un truc a été ajouté
     */
    protected static boolean appendBeehiveContent(List<Text> info, ItemStack stack) {
        // Cas où on quitte direct
        if (info.size() >= MAX_LINES
                || !(stack.getItem() == Items.BEE_NEST
                        || stack.getItem() == Items.BEEHIVE)) {
            return false;
        }

        // Récupération
        final CompoundTag blockEntityTag = stack.getSubTag("BlockEntityTag");
        if (blockEntityTag == null) return false;

        final int beeCount = blockEntityTag.getList("Bees", 10).size(); // 10: CompoundTag
        if (beeCount == 0) return false;

        // Ajout
        info.add(
            new TranslatableText("entity.minecraft.bee")
            .append(" x" + beeCount)
            .formatted(Formatting.GRAY)
        );
        return true;
    }

    /**
     * Ajoute la commande d'un commande block aux infos.
     * @param info Infos
     * @param stack Stack de l'item (avec un tag)
     * @return `true` si un truc a été ajouté
     */
    protected static boolean appendCommandBlockInfo(List<Text> info, ItemStack stack) {
        // Cas où on quitte direct
        if (info.size() >= MAX_LINES
                || !(stack.getItem() instanceof CommandBlockItem)) {
            return false;
        }

        // Récupération
        final CompoundTag blockEntityTag = stack.getSubTag("BlockEntityTag");
        if (blockEntityTag == null) return false;

        String command = blockEntityTag.getString("Command").trim();
        if (command.isEmpty()) return false;

        // Troncage
        final int maxLines = Math.min(MAX_LINES - info.size(), MAX_COMMAND_LINES);
        final int maxLength = (int) (maxLines * MAX_COMMAND_LINE_LENGTH * 1.125);
        final boolean shouldCut = command.length() > maxLength;
        if (shouldCut) {
            command = command.substring(0, maxLength);
        }

        // Découpage en morceaux
        List<Text> lines = Texts.wrapLines(
            new LiteralText(command),
            MAX_COMMAND_LINE_LENGTH * 6,
            textRenderer,
            false, false
        );

        // Retroncage
        if (shouldCut || lines.size() > maxLines) {
            if (lines.size() > maxLines) lines = lines.subList(0, maxLines);
            lines.get(maxLines - 1).append("...");
        }

        // Formattage
        for (Text text : lines) text.formatted(Formatting.GRAY);

        // Ajout
        return appendToInfo(info, lines);
    }

    /**
     * Ajoute le contenu d'un conteneur aux infos de l'item.
     * @param info Infos
     * @param stack Stack de l'item (avec un tag)
     * @return `true` si un truc a été ajouté
     * @see net.minecraft.block.ShulkerBoxBlock#buildTooltip ShulkerBoxBlock.buildTooltip
     * @see net.minecraft.inventory.Inventories#fromTag Inventories.fromTag
     */
    protected static boolean appendContainerContent(List<Text> info, ItemStack stack) {
        // Cas où on quitte direct
        if (info.size() >= MAX_LINES) {
            return false;
        }

        final CompoundTag tag = stack.getSubTag("BlockEntityTag");
        if (tag == null) return false;

        final ListTag items = tag.getList("Items", 10); // 10: CompoundTag

        List<Text> newInfo = new ArrayList<>(MAX_LINES - info.size());

        // Loot table
        if (tag.contains("LootTable", 8)) newInfo.add(new LiteralText("???????"));

        // Items
        if (!items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                // Récupération de l'item
                final CompoundTag itemTag = items.getCompound(i);
                if (itemTag.isEmpty()) continue;
                final ItemStack iStack = ItemStack.fromTag(itemTag);
                if (iStack.isEmpty()) continue;

                if (info.size() + newInfo.size() < MAX_LINES) {
                    // Ajout du nom si pas encore plein
                    newInfo.add(
                        iStack.getName()
                        .append(" x" + iStack.getCount())
                        .formatted(Formatting.GRAY)
                    );
                } else {
                    // Ajout de null pcq sera recoupé
                    // + pour que le "and x more" soit bon
                    newInfo.add(null);
                }
            }
        }

        return appendToInfo(info, newInfo);
    }

    /**
     * Ajoute les textes des enchantements d'un item aux infos.
     * @param info Infos
     * @param stack Stack de l'item (avec un tag)
     * @return `true` si un truc a été ajouté
     */
    protected static boolean appendEnchantments(List<Text> info, ItemStack stack) {
        // Cas où on quitte direct
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

    /**
     * Ajoute les effets de potion aux infos de l'item.
     * @param info Infos
     * @param stack Stack de l'item (avec un tag)
     * @return `true` si un truc a été ajouté
     */
    protected static boolean appendPotionEffects(List<Text> info, ItemStack stack) {
        List<Text> potionInfo = new ArrayList<>(MAX_LINES);

        do {
            if (appendUsualTooltip(potionInfo, stack, PotionItem.class)) break;
            if (appendUsualTooltip(potionInfo, stack, LingeringPotionItem.class)) break;
            if (appendUsualTooltip(potionInfo, stack, TippedArrowItem.class)) break;

            return false; // On arrive ici = c'est aucun des 3
        } while (false);

        // On enlève les lignes "Quand bu : bla bla bla" (si elles y sont)
        for (int i = potionInfo.size() - 1; i >= 0; i--) {
            final Text line = potionInfo.get(i);
            if (line instanceof LiteralText && line.asString().equals("")) {
                // Si c'est la ligne vide, on enlève tout ce qui suit et fini
                potionInfo = potionInfo.subList(0, i);
                break;
            }
        }

        // Fini
        return appendToInfo(info, potionInfo);
    }

    /**
     * Ajoute le texte d'un panneau aux infos.
     * @param info Infos
     * @param stack Stack de l'item (avec un tag)
     * @return `true` si un truc a été ajouté
     */
    protected static boolean appendSignText(List<Text> info, ItemStack stack) {
        // Cas où on quitte direct
        if (info.size() >= MAX_LINES
                || !(stack.getItem() instanceof SignItem)) {
            return false;
        }

        // Récupération
        final CompoundTag blockEntityTag = stack.getSubTag("BlockEntityTag");
        if (blockEntityTag == null) return false;

        final List<Text> lines = new ArrayList<>(4);

        // Ajout
        for (int i = 0; i < 4; i++) {
            Text text;

            try {
                text = Text.Serializer.fromJson(
                    blockEntityTag.getString("Text" + (i + 1))
                );
            } catch (JsonParseException e) {
                continue;
            }

            System.out.println("yyyy" + text);

            if (text == null) continue;
            final String str = text.asString();
            System.out.println("yyyy" + str);
            if (str.trim().isEmpty()) continue;

            // Remplissage des lignes d'avant (si vides) + ajout
            while (lines.size() < i) lines.add(new LiteralText(""));
            lines.add(new LiteralText(str));
        }

        for (Text text : lines) text.formatted(Formatting.GRAY);

        return appendToInfo(info, lines);
    }

    /**
     * Ajoute 'incassable' aux infos si l'item l'est.
     * @param info Infos
     * @param stack Stack de l'item (avec un tag)
     * @return `true` si un truc a été ajouté
     */
    protected static boolean appendUnbreakable(List<Text> info, ItemStack stack) {
        // Cas où on quitte direct
        if (info.size() >= MAX_LINES
                || !stack.getItem().isDamageable()) {
            return false;
        }

        // Récupération + ajout
        final CompoundTag tag = stack.getTag();

        if (!tag.getBoolean("Unbreakable")
                || (tag.getInt("HideFlags") & 4) != 0) {
            return false;
        }

        info.add(
            new TranslatableText("item.unbreakable")
            .formatted(Formatting.BLUE)
        );

        return true;
    }



    /** Classe pour stocker les informations d'une seule ligne pour éviter
     * de calculer les même trucs plusieurs fois */
    public static class InfoLine {

        public final String formatted;
        public final int width;

        InfoLine(Text text) {
            formatted = text.asFormattedString();
            width = textRenderer.getStringWidth(formatted);
        }
    }

}
