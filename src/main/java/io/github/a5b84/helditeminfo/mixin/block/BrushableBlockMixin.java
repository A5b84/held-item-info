package io.github.a5b84.helditeminfo.mixin.block;

import io.github.a5b84.helditeminfo.Appenders;
import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import io.github.a5b84.helditeminfo.mixin.BrushableBlockEntityAccessor;
import net.minecraft.block.BrushableBlock;
import net.minecraft.component.DataComponentTypes;
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
        //noinspection deprecation (getNbt)
        builder.getComponentForDisplay(DataComponentTypes.BLOCK_ENTITY_DATA)
                .flatMap(blockEntityData -> blockEntityData.getNbt().getCompound(BrushableBlockEntityAccessor.getItemNbtKey()))
                .ifPresent(itemNbt -> Appenders.appendItem(builder, itemNbt));
    }
}
