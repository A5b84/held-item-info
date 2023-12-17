package io.github.a5b84.helditeminfo.mixin.block;

import io.github.a5b84.helditeminfo.Appenders;
import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import net.minecraft.block.BrushableBlock;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

@Mixin(BrushableBlock.class)
public abstract class BrushableBlockMixin implements TooltipAppender {

    @Override
    public boolean heldItemInfo_shouldAppendTooltip() {
        return config.showContainerContent();
    }

    @Override
    public void heldItemInfo_appendTooltip(TooltipBuilder builder) {
        NbtCompound blockEntityTag = builder.stack.getSubNbt("BlockEntityTag");
        if (blockEntityTag != null) {
            Appenders.appendItem(builder, blockEntityTag.getCompound("item"));
        }
    }

}
