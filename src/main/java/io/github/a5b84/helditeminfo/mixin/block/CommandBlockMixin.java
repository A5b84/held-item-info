package io.github.a5b84.helditeminfo.mixin.block;

import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import io.github.a5b84.helditeminfo.Util;
import net.minecraft.block.CommandBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.text.MutableText;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

@Mixin(CommandBlock.class)
public abstract class CommandBlockMixin implements TooltipAppender {

    @Override
    public boolean heldItemInfo_shouldAppendTooltip() {
        return config.showCommandBlockInfo();
    }

    @Override
    public void heldItemInfo_appendTooltip(TooltipBuilder builder) {
        NbtComponent blockEntityData = builder.stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
        if (blockEntityData == null) return;

        //noinspection deprecation (getNbt)
        String command = blockEntityData.getNbt().getString("Command");
        if (command == null) return;

        command = command.trim();
        if (command.isEmpty()) return;

        int maxLines = Math.min(config.maxCommandLines(), builder.getRemainingLines());
        List<MutableText> lines = Util.wrapLines(command, maxLines);

        for (MutableText text : lines) {
            builder.append(() -> text.formatted(TooltipBuilder.DEFAULT_COLOR));
        }
    }
}
