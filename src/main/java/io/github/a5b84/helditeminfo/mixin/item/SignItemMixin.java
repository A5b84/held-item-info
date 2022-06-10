package io.github.a5b84.helditeminfo.mixin.item;

import com.google.gson.JsonParseException;
import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import net.minecraft.item.SignItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.List;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

@Mixin(SignItem.class)
public abstract class SignItemMixin implements TooltipAppender {

    @Override
    public boolean heldItemInfo_shouldAppendTooltip() {
        return config.showSignText();
    }

    @Override
    public void heldItemInfo_appendTooltip(TooltipBuilder builder) {
        // Get the text
        NbtCompound blockEntityTag = builder.stack.getSubNbt("BlockEntityTag");
        if (blockEntityTag != null) {

            List<MutableText> lines = new ArrayList<>(4);

            // Add it
            for (int i = 0; i < 4; i++) {
                MutableText text;

                try {
                    String textJson = blockEntityTag.getString("Text" + (i + 1));
                    text = MutableText.Serializer.fromJson(textJson);
                } catch (JsonParseException e) {
                    continue;
                }

                if (text == null) continue;

                String str = text.getString();
                if (str.isBlank()) continue;

                // Add empty lines up to the current one
                while (lines.size() < i) lines.add(Text.empty());
                lines.add(Text.literal(str));
            }

            // Formatting
            for (MutableText line : lines) {
                line.formatted(TooltipBuilder.DEFAULT_COLOR);
            }

            builder.appendAll(lines);
        }
    }

}
