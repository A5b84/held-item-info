package io.github.a5b84.helditeminfo.mixin.item;

import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

@Mixin(FilledMapItem.class)
public abstract class FilledMapItemMixin implements TooltipAppender {

    @Override
    public boolean heldItemInfo_shouldAppendTooltip() {
        return config.showFilledMapId();
    }

    /**
     * @see ItemStack#getTooltip
     * @see FilledMapItem#appendTooltip
     */
    @Override
    public void heldItemInfo_appendTooltip(TooltipBuilder builder) {
        Integer id = FilledMapItem.getMapId(builder.stack);
        if (id != null) {
            builder.append(Text.literal("#" + id).formatted(TooltipBuilder.DEFAULT_COLOR));
        } else {
            builder.append(Text.translatable("filled_map.unknown").formatted(TooltipBuilder.DEFAULT_COLOR));
        }
    }
}
