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

import static io.github.a5b84.helditeminfo.Mod.FONT_HEIGHT;
import static io.github.a5b84.helditeminfo.Mod.config;

@Mixin(InGameHud.class)
public abstract class HeldItemTooltipMixin extends DrawableHelper {

    @Shadow private @Final MinecraftClient client;
    @Shadow private int heldItemTooltipFade;
    @Shadow private ItemStack currentStack;
    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;



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
        y = scaledHeight - 50 - FONT_HEIGHT - (int) ((config.lineHeight() - config.offsetPerExtraLine()) * (info.size() - 1));
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
            (scaledWidth + maxWidth) / 2 + 2, y + (config.lineHeight() * info.size()) + 2,
            color
        );
    }



    /** Replaces vanilla rendering with the mod's */
    @Redirect(
        method = "renderHeldItemTooltip",
        at = @At(value = "INVOKE", target = "net/minecraft/client/font/TextRenderer.drawWithShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I")
    )
    private int drawTextProxy(TextRenderer fontRenderer, MatrixStack stack, Text name, float _x, float _y, int color) {
        final int lineHeight = config.lineHeight();
        for (final InfoLine line : info) {
            final int x = (scaledWidth - line.width) / 2;
            fontRenderer.drawWithShadow(stack, line.text, x, y, color);
            y += lineHeight;
        }

        return 0;
    }



    @Inject(method = "tick", at = @At("HEAD"))
    public void onBeforeTick(CallbackInfo ci) {
        stackBeforeTick = currentStack;
    }

    /** Rebuilds the tooltip */
    @Inject(method = "tick", at = @At("RETURN"))
    public void onAfterTick(CallbackInfo ci) {
        if (client.player == null || currentStack == stackBeforeTick) {
            return;
        }

        if (currentStack.isEmpty()) {
            info = Collections.emptyList();
            return;
        }

        // New tooltip
        List<Text> newInfo = InfoBuilder.buildInfo(currentStack);

        // Reset everything if the tooltip changed
        if (InfoBuilder.areEqual(info, newInfo)) return;

        info = InfoBuilder.toInfoLines(newInfo);
        maxWidth = -1;
        heldItemTooltipFade = (int) (20 * (config.baseFadeDuration() + config.fadeDurationPerExtraLine() * (info.size() - 1)));
    }

}
