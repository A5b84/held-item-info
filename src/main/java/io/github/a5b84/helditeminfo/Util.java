package io.github.a5b84.helditeminfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class Util {

  /**
   * @see Font#lineHeight
   */
  public static final int FONT_HEIGHT = 9;

  private static final double CHARACTER_WIDTH = 6;

  private Util() {}

  public static MutableComponent withDefaultColor(Component test) {
    if (test instanceof MutableComponent mutableText) {
      return mutableText.withStyle(TooltipBuilder.DEFAULT_COLOR);
    } else {
      return test.copy().withStyle(TooltipBuilder.DEFAULT_COLOR);
    }
  }

  public static void setAllToDefaultColor(List<Component> lines) {
    for (Component line : lines) {
      if (line instanceof MutableComponent mutableLine) {
        mutableLine.withStyle(TooltipBuilder.DEFAULT_COLOR);
      }
    }
  }

  public static boolean isBlank(FormattedText visitable) {
    return visitable
        .visit(
            asString -> {
              if (asString.isBlank()) {
                return Optional.empty();
              } else {
                return Optional.of(0);
              }
            })
        .isEmpty();
  }

  /**
   * @param s The string to wrap
   * @param maxLines The max number of lines the string will be wrapped into
   * @see ComponentRenderUtils#wrapComponents
   * @see StringSplitter#splitLines(String, int, Style)
   */
  public static List<MutableComponent> wrapLines(String s, int maxLines) {
    // Shortening
    if (maxLines <= 0) return Collections.emptyList();

    double maxLength = 1.25 * maxLines * HeldItemInfo.config.maxLineLength();
    // ^ add 25% to avoid truncating too much when using many narrow characters (e.g. 'i')
    if (maxLength <= 0) return Collections.emptyList();
    if (maxLength > Integer.MAX_VALUE) maxLength = Integer.MAX_VALUE;
    // ^ In case the user messed with their config (could crash)

    boolean wasShortened = s.length() > maxLength;
    if (wasShortened) s = s.substring(0, (int) maxLength);

    // Splitting
    final List<MutableComponent> lines = new ArrayList<>(maxLines);
    final String finalString = s; // final copy to refer to it in the lambda
    double maxWidth = HeldItemInfo.config.maxLineLength() * CHARACTER_WIDTH;
    if (maxWidth > Integer.MAX_VALUE) maxWidth = Integer.MAX_VALUE;
    //      ^ In case the user messed with their config (could crash)
    Font textRenderer = Minecraft.getInstance().font;
    textRenderer
        .getSplitter()
        .splitLines(
            s,
            (int) maxWidth,
            Style.EMPTY,
            false,
            (style, start, end) -> lines.add(Component.literal(finalString.substring(start, end))));

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
   * @see ComponentRenderUtils#wrapComponents
   * @see StringSplitter#splitLines(String, int, Style)
   */
  public static List<MutableComponent> wrapLines(FormattedText s, int maxLines) {
    // TODO somehow make this not yeet styles
    if (maxLines <= 0) return Collections.emptyList();

    double maxWidth = HeldItemInfo.config.maxLineLength() * CHARACTER_WIDTH;
    if (maxWidth > Integer.MAX_VALUE) maxWidth = Integer.MAX_VALUE;
    //      ^ In case the user messed with their config (could crash)
    Font textRenderer = Minecraft.getInstance().font;
    List<FormattedText> strings =
        textRenderer.getSplitter().splitLines(s, (int) maxWidth, Style.EMPTY);

    // Convert and truncate
    List<MutableComponent> lines = new ArrayList<>(maxLines);
    for (FormattedText visitable : strings) {
      lines.add(Component.literal(visitable.getString()));
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
