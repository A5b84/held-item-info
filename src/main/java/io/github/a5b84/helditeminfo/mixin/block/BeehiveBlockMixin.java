package io.github.a5b84.helditeminfo.mixin.block;

import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

@Mixin(BeehiveBlock.class)
public abstract class BeehiveBlockMixin implements TooltipAppender {

    @Override
    public boolean heldItemInfo_shouldAppendTooltip() {
        return config.showBeehiveContent();
    }

    @Override
    public void heldItemInfo_appendTooltip(TooltipBuilder builder) {
        NbtCompound blockEntityTag = builder.stack.getSubNbt("BlockEntityTag");
        if (blockEntityTag != null) {
            int beeCount = blockEntityTag.getList("Bees", NbtType.COMPOUND).size();
            if (beeCount > 0) {
                Text text = Text.translatable("entity.minecraft.bee")
                        .append(" x" + beeCount)
                        .formatted(TooltipBuilder.DEFAULT_COLOR);
                builder.append(text);
            }
        }
    }

}
