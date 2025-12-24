package io.github.a5b84.helditeminfo.mixin;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface GuiAccessor {

  @Accessor("NUM_HEARTS_PER_ROW")
  static int getNumHeartsPerRow() {
    throw new AssertionError();
  }

  @Accessor("LINE_HEIGHT")
  static int getLineHeight() {
    throw new AssertionError();
  }
}
