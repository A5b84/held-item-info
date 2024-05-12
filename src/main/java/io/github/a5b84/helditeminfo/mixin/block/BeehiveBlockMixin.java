package io.github.a5b84.helditeminfo.mixin.block;

import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

@Mixin(BeehiveBlock.class)
public abstract class BeehiveBlockMixin implements TooltipAppender {

    @Override
    public boolean heldItemInfo_shouldAppendTooltip() {
        return config.showBeehiveContent();
    }

    @Override
    public void heldItemInfo_appendTooltip(TooltipBuilder builder) {
        List<BeehiveBlockEntity.BeeData> beeData = builder.stack.get(DataComponentTypes.BEES);

        if (beeData != null && !beeData.isEmpty()) {
            builder.append(Text.translatable("container.shulkerBox.itemCount", Text.translatable("entity.minecraft.bee"), beeData.size()).formatted(TooltipBuilder.DEFAULT_COLOR));
        }
    }

}
