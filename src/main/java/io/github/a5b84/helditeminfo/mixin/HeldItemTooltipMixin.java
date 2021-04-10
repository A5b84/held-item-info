package io.github.a5b84.helditeminfo.mixin;

import io.github.a5b84.helditeminfo.InfoBuilder;
import io.github.a5b84.helditeminfo.InfoBuilder.InfoLine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(InGameHud.class)
public abstract class HeldItemTooltipMixin extends DrawableHelper {

    @Shadow private @Final MinecraftClient client;
    @Shadow private int heldItemTooltipFade;
    @Shadow private ItemStack currentStack;
    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;



    @Unique private static final int FONT_HEIGHT = 9;
    @Unique private static final int LINE_HEIGHT = FONT_HEIGHT - 1;
    @Unique private static final float OFFSET_PER_EXTRA_LINE = LINE_HEIGHT - .334f;

    @Unique private List<InfoLine> info = Collections.emptyList();
    @Unique private int y;
    private ItemStack stackBeforeTick;

    /** Width of the longest line, or some negative number if not computed yet */
    private int maxWidth = -1;



    /** Updates rendering variables */
    @Inject(
        method = "renderHeldItemTooltip",
        at = @At(value = "INVOKE", target = "net/minecraft/client/font/TextRenderer.getWidth(Lnet/minecraft/text/StringVisitable;)I")
        // Not injecting at head because the tooltip may not be rendered anyway
    )
    public void onBeforeRenderHeldItemTooltip(CallbackInfo ci) {
        // 50 = 32 (hotbar) + 14 (health & xp) + 4 (spacing) (?)
        y = scaledHeight - 50 - FONT_HEIGHT - (int) (OFFSET_PER_EXTRA_LINE * (info.size() - 1));
        //noinspection ConstantConditions
        if (!this.client.interactionManager.hasStatusBars()) y += 14;
    }



    /** Renders the background if enabled in the vanilla settings */
    @Redirect(
        method = "renderHeldItemTooltip",
        at = @At(value = "INVOKE", target = "net/minecraft/client/gui/hud/InGameHud.fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V")
    )
    private void fillBackgroundProxy(MatrixStack stack, int x1, int y1, int x2, int y2, int color) {
        // Only do the math part when actually rendering
        if ((color & 0xff000000) == 0) return;

        // Find the largest line
        if (maxWidth < 0) {
            for (InfoLine line : info) {
                if (line.width > maxWidth) maxWidth = line.width;
            }
        }

        // Fill the background
        fill(
            stack,
            (scaledWidth - maxWidth) / 2 - 2, y - 2,
            (scaledWidth + maxWidth) / 2 + 2, y + (LINE_HEIGHT * info.size()) + 2,
            color
        );
    }



    /** Replaces vanilla rendering with the mod's */
    @Redirect(
        method = "renderHeldItemTooltip",
        at = @At(value = "INVOKE", target = "net/minecraft/client/font/TextRenderer.drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I")
    )
    private int drawTextProxy(TextRenderer fontRenderer, MatrixStack stack, Text name, float _x, float _y, int color) {
        for (final InfoLine line : info) {
            final int x = (scaledWidth - line.width) / 2;
            fontRenderer.drawWithShadow(stack, line.text, x, y, color);
            y += LINE_HEIGHT;
        }

        return 0;
    }



    @Inject(method = "tick", at = @At("HEAD"))
    public void onBeforeTick(CallbackInfo ci) {
        if (this.client.player != null) {
            stackBeforeTick = currentStack;
        }
    }

    /** Rebuilds the tooltip */
    @Inject(method = "tick", at = @At("RETURN"))
    public void onAfterTick(CallbackInfo ci) {
        if (this.client.player == null
                || currentStack == stackBeforeTick
                || currentStack.isEmpty()) {
            return;
        }

        // New tooltip
        List<Text> newInfo = InfoBuilder.buildInfo(currentStack);

        // Reset everything if the tooltip changed
        if (InfoBuilder.areEqual(info, newInfo)) return;

        info = InfoBuilder.toInfoLines(newInfo);
        maxWidth = -1;
        heldItemTooltipFade = 40 + 4 * (info.size() - 1);
    }

}
