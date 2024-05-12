package io.github.a5b84.helditeminfo.mixin;

import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShulkerBoxBlock.class)
public interface ShulkerBoxBlockAccessor {
    @Accessor("UNKNOWN_CONTENTS_TEXT")
    static Text getUnknownContentsText() {
        throw new AssertionError();
    }
}
