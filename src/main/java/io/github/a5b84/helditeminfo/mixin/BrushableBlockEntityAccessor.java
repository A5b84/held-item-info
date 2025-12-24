package io.github.a5b84.helditeminfo.mixin;

import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BrushableBlockEntity.class)
public interface BrushableBlockEntityAccessor {
  @Accessor("ITEM_TAG")
  static String getItemTagKey() {
    throw new AssertionError();
  }
}
