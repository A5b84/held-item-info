package io.github.a5b84.helditeminfo.mixin.item;

import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
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
        builder.append(() -> {
            MapIdComponent mapIdComponent = builder.stack.get(DataComponentTypes.MAP_ID);
            MutableText text;
            if (mapIdComponent != null) {
                text = FilledMapItem.getIdText(mapIdComponent).copy();
            } else {
                text = Text.translatable("filled_map.unknown");
            }
            return text.formatted(TooltipBuilder.DEFAULT_COLOR);
        });
    }
}
