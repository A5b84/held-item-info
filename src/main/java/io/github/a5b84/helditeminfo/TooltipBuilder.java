package io.github.a5b84.helditeminfo;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

public class TooltipBuilder {

    public static final Formatting DEFAULT_COLOR = Formatting.GRAY;

    public final ItemStack stack;
    public final Item.TooltipContext tooltipContext;
    private final int maxSize;
    private final List<Text> lines;
    /**
     * Real number of lines including the ones that are hidden because they
     * would exceed {@link #maxSize}.
     */
    private int realSize = 0;


    public TooltipBuilder(ItemStack stack, Item.TooltipContext tooltipContext, int maxSize) {
        this.stack = stack;
        this.tooltipContext = tooltipContext;
        this.maxSize = maxSize;
        lines = new ArrayList<>(maxSize);
    }


    /**
     * @return {@code true} iff at least one more line can be added.
     */
    public boolean canAdd() {
        return lines.size() < maxSize;
    }

    public int getRemainingLines() {
        return maxSize - lines.size();
    }


    public void append(Text text) {
        realSize++;
        if (canAdd()) {
            lines.add(text);
        }
    }

    public void append(Supplier<Text> textSupplier) {
        realSize++;
        if (canAdd()) {
            lines.add(textSupplier.get());
        }
    }

    public void appendAll(List<? extends Text> newLines) {
        realSize += newLines.size();

        if (canAdd()) {
            for (Text line : newLines) {
                lines.add(line);
                if (!canAdd()) break;
            }
        }
    }


    public List<Text> build() {
        if (realSize > maxSize && config.showHiddenLinesCount()) {
            Text moreText = Text.translatable("container.shulkerBox.more", realSize - maxSize + 1)
                    .formatted(DEFAULT_COLOR, Formatting.ITALIC);
            lines.set(lines.size() - 1, moreText);
        }

        return lines;
    }

}
