package io.github.a5b84.helditeminfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

/** A single line in the held item tooltip. */
public class TooltipLine {

  public final Text text;
  public final int width;

  public TooltipLine(TextRenderer textRenderer, Text text) {
    this.text = text;
    width = textRenderer.getWidth(text);
  }

  public static List<TooltipLine> from(List<Text> texts) {
    List<TooltipLine> result = new ArrayList<>(texts.size());
    TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
    for (Text text : texts) {
      result.add(new TooltipLine(textRenderer, text));
    }

    return result;
  }

  public static boolean areEquivalent(List<TooltipLine> lines, List<Text> texts) {
    if (lines.size() != texts.size()) return false;

    Iterator<TooltipLine> lineIt = lines.iterator();
    Iterator<Text> textIt = texts.iterator();

    while (lineIt.hasNext()) { // Same length, no need to check both hasNext()
      if (!lineIt.next().text.equals(textIt.next())) {
        return false;
      }
    }

    return true;
  }
}
