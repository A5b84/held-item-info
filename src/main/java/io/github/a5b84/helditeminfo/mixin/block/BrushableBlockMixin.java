package io.github.a5b84.helditeminfo.mixin.block;

import io.github.a5b84.helditeminfo.Appenders;
import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import net.minecraft.block.BrushableBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
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
        NbtComponent blockEntityData = builder.stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
        if (blockEntityData != null) {
            //noinspection deprecation (getNbt)
            Appenders.appendItem(builder, blockEntityData.getNbt().getCompound("item"));
        }
    }

}
