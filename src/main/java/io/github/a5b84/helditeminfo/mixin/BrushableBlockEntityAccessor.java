package io.github.a5b84.helditeminfo.mixin;

import net.minecraft.block.entity.BrushableBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BrushableBlockEntity.class)
public interface BrushableBlockEntityAccessor {
  @Accessor("ITEM_NBT_KEY")
  static String getItemNbtKey() {
    throw new AssertionError();
  }
}
