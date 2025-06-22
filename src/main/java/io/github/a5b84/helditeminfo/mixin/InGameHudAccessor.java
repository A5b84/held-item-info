package io.github.a5b84.helditeminfo.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InGameHud.class)
public interface InGameHudAccessor {

    @Accessor("field_32169")
    static int getNumHeartsPerRow() {
        throw new AssertionError();
    }

    @Accessor("field_32170")
    static int getLineHeight() {
        throw new AssertionError();
    }
}
