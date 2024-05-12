package io.github.a5b84.helditeminfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.MutableText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

public final class Util {

    public static final int FONT_HEIGHT = 9;
    private static final double CHARACTER_WIDTH = 6;

    private Util() {}


    public static void setAllToDefaultColor(List<Text> lines) {
        for (Text line : lines) {
            if (line instanceof MutableText mutableLine) {
                mutableLine.formatted(TooltipBuilder.DEFAULT_COLOR);
            }
        }
    }

    public static boolean isBlank(StringVisitable visitable) {
        Optional<Boolean> result = visitable.visit(asString -> {
            if (asString.isBlank()) {
                return Optional.empty();
            } else {
                return Optional.of(false);
            }
        });

        return result.isEmpty();
    }


    /**
     * @param s The string to wrap
     * @param maxLines The max number of lines the string will be wrapped into
     * @see ChatMessages#breakRenderedChatMessageLines
     * @see TextHandler#wrapLines(String, int, Style) */
    public static List<MutableText> wrapLines(String s, int maxLines) {
        // Shortening
        if (maxLines <= 0) return Collections.emptyList();

        double maxLength = 1.25 * maxLines * config.maxLineLength();
        // ^ add 25% to avoid truncating too much when using many narrow characters (e.g. 'i')
        if (maxLength <= 0) return Collections.emptyList();
        if (maxLength > Integer.MAX_VALUE) maxLength = Integer.MAX_VALUE;
        // ^ In case the user messed with their config (could crash)

        boolean wasShortened = s.length() > maxLength;
        if (wasShortened) s = s.substring(0, (int) maxLength);

        // Splitting
        final List<MutableText> lines = new ArrayList<>(maxLines);
        final String finalString = s; // final copy to refer to it in the lambda
        double maxWidth = config.maxLineLength() * CHARACTER_WIDTH;
        if (maxWidth > Integer.MAX_VALUE) maxWidth = Integer.MAX_VALUE;
        //      ^ In case the user messed with their config (could crash)
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        textRenderer.getTextHandler().wrapLines(
                s, (int) maxWidth, Style.EMPTY, false,
                (style, start, end) -> lines.add(Text.literal(finalString.substring(start, end)))
        );

        // Truncating
        if (lines.size() > maxLines) {
            lines.subList(maxLines, lines.size()).clear();
            wasShortened = true;
        }

        if (wasShortened) {
            lines.getLast().append("...");
        }

        // Done
        return lines;
    }

    /**
     * @see ChatMessages#breakRenderedChatMessageLines
     * @see TextHandler#wrapLines(String, int, Style) */
    public static List<MutableText> wrapLines(StringVisitable s, int maxLines) {
        // TODO somehow make this not yeet styles
        if (maxLines <= 0) return Collections.emptyList();

        double maxWidth = config.maxLineLength() * CHARACTER_WIDTH;
        if (maxWidth > Integer.MAX_VALUE) maxWidth = Integer.MAX_VALUE;
        //      ^ In case the user messed with their config (could crash)
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        List<StringVisitable> strings = textRenderer.getTextHandler().wrapLines(s, (int) maxWidth, Style.EMPTY);

        // Convert and truncate
        List<MutableText> lines = new ArrayList<>(maxLines);
        for (StringVisitable visitable : strings) {
            lines.add(Text.literal(visitable.getString()));
            if (lines.size() >= maxLines) {
                if (strings.size() > maxLines) {
                    lines.get(maxLines - 1).append("...");
                }
                break;
            }
        }

        return lines;
    }

}
