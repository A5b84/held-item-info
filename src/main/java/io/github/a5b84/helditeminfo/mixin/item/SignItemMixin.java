package io.github.a5b84.helditeminfo.mixin.item;

import com.google.gson.JsonParseException;
import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import net.minecraft.item.SignItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Collections;
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
            List<MutableText> frontLines = buildSide(blockEntityTag, "front_text");
            List<MutableText> backLines = buildSide(blockEntityTag, "back_text");

            if (!frontLines.isEmpty()) {
                builder.appendAll(frontLines);
                if (!backLines.isEmpty()) {
                    builder.append(Text.literal("-------").formatted(TooltipBuilder.DEFAULT_COLOR));
                }
            }

            if (!backLines.isEmpty()) {
                builder.appendAll(backLines);
            }
        }
    }

    @Unique
    private List<MutableText> buildSide(NbtCompound blockEntityTag, String sideKey) {
        NbtList messages = blockEntityTag.getCompound(sideKey).getList("messages", NbtElement.STRING_TYPE);
        if (messages.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<MutableText> lines = new ArrayList<>(4);
            int i = -1;

            for (NbtElement element : messages) {
                i++;

                if (element instanceof NbtString message) {
                    MutableText text;
                    try {
                        String textJson = message.asString();
                        text = MutableText.Serializer.fromJson(textJson);
                    } catch (JsonParseException e) {
                        continue;
                    }

                    if (text == null) continue;

                    String str = text.getString();
                    if (str.isBlank()) continue;

                    // Add empty lines up to the current one
                    if (!lines.isEmpty()) {
                        while (lines.size() < i) lines.add(Text.empty());
                    }
                    lines.add(Text.literal(str));
                }
            }

            // Formatting
            for (MutableText line : lines) {
                line.formatted(TooltipBuilder.DEFAULT_COLOR);
            }

            return lines;
        }
    }

}
