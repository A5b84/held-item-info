package io.github.a5b84.helditeminfo.mixin;

import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import io.github.a5b84.helditeminfo.TooltipLine;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

import static io.github.a5b84.helditeminfo.Appenders.appendContainerContent;
import static io.github.a5b84.helditeminfo.Appenders.appendEnchantments;
import static io.github.a5b84.helditeminfo.Appenders.appendLore;
import static io.github.a5b84.helditeminfo.Appenders.appendUnbreakable;
import static io.github.a5b84.helditeminfo.HeldItemInfo.config;
import static io.github.a5b84.helditeminfo.Util.FONT_HEIGHT;

@Mixin(InGameHud.class)
public abstract class HeldItemTooltipMixin {

    @Shadow private @Final MinecraftClient client;
    @Shadow private int heldItemTooltipFade;
    @Shadow private ItemStack currentStack;
    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;


    @Unique private List<TooltipLine> tooltip = Collections.emptyList();
    @Unique private int y;
    @Unique private ItemStack stackBeforeTick;

    /** Width of the longest line, or some negative number if not computed yet */
    @Unique private int maxWidth = -1;


    /** Updates rendering variables */
    @Inject(method = "renderHeldItemTooltip",
            at = @At(value = "INVOKE", target = "net/minecraft/client/font/TextRenderer.getWidth(Lnet/minecraft/text/StringVisitable;)I"))
    public void onBeforeRenderHeldItemTooltip(CallbackInfo ci) {
        y = scaledHeight - 50 - FONT_HEIGHT // Vanilla value (50 = 32 (hotbar) + 14 (health & xp) + 4 (spacing))
                - (int) ((config.lineHeight() - config.offsetPerExtraLine()) * (tooltip.size() - 1))
                - config.verticalOffset();
        //noinspection ConstantConditions
        if (!this.client.interactionManager.hasStatusBars()) y += 14;
    }


    /** Renders the background if enabled in the vanilla settings */
    @Redirect(method = "renderHeldItemTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V"))
    private void fillBackgroundProxy(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        // Only do the math part when actually rendering
        if ((color & 0xff000000) == 0) return;

        // Find the longest line
        if (maxWidth < 0) {
            for (TooltipLine line : tooltip) {
                if (line.width > maxWidth) {
                    maxWidth = line.width;
                }
            }
        }

        // Fill the background
        context.fill(
            (scaledWidth - maxWidth) / 2 - 2, y - 2,
            (scaledWidth + maxWidth) / 2 + 2, y + (config.lineHeight() * tooltip.size()) + 2,
            color
        );
    }


    /** Replaces vanilla rendering with the mod's */
    @Redirect(method = "renderHeldItemTooltip",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"))
    private int drawTextProxy(DrawContext context, TextRenderer textRenderer, Text name, int _x, int _y, int color) {
        int lineHeight = config.lineHeight();
        for (TooltipLine line : tooltip) {
            int x = (scaledWidth - line.width) / 2;
            context.drawTextWithShadow(textRenderer, line.text, x, y, color);
            y += lineHeight;
        }

        return 0;
    }


    @Inject(method = "tick()V", at = @At("HEAD"))
    public void onBeforeTick(CallbackInfo ci) {
        stackBeforeTick = currentStack;
    }

    /** Rebuilds the tooltip */
    @Inject(method = "tick()V", at = @At("RETURN"))
    public void onAfterTick(CallbackInfo ci) {
        if (client.player == null || currentStack == stackBeforeTick) {
            return;
        }

        // New tooltip
        if (currentStack.isEmpty()) {
            tooltip = Collections.emptyList();
        } else {
            List<Text> newInfo = buildTooltip(currentStack);

            // Reset everything if the tooltip changed
            if (!TooltipLine.areEquivalent(tooltip, newInfo)) {
                tooltip = TooltipLine.from(newInfo);
                maxWidth = -1;
                heldItemTooltipFade = (int) (SharedConstants.TICKS_PER_SECOND * (config.baseFadeDuration() + config.fadeDurationPerExtraLine() * (tooltip.size() - 1)));
            }
        }
    }

    @Unique
    private List<Text> buildTooltip(ItemStack stack) {
        if (stack.isEmpty()) {
            return Collections.emptyList();
        } else {
            TooltipBuilder builder = new TooltipBuilder(stack, config.maxLines());

            // Stack name
            if (config.showName()) {
                MutableText stackName = Text.empty() // Prevents overwriting the name formatting
                        .append(stack.getName())
                        .formatted(stack.getRarity().formatting);
                if (stack.hasCustomName()) stackName.formatted(Formatting.ITALIC);
                builder.append(stackName);
            }

            // Item-specific tooltip
            Item item = stack.getItem();
            if (item instanceof TooltipAppender appender
                    && appender.heldItemInfo_shouldAppendTooltip()) {
                appender.heldItemInfo_appendTooltip(builder);
            }

            if (item instanceof BlockItem blockItem
                    && blockItem.getBlock() instanceof TooltipAppender appender
                    && appender.heldItemInfo_shouldAppendTooltip()) {
                appender.heldItemInfo_appendTooltip(builder);
            }

            // Tag-related lines
            if (stack.hasNbt()) {
                if (config.showEnchantments()) appendEnchantments(builder);
                if (config.showContainerContent()) appendContainerContent(builder);
                if (config.showLore()) appendLore(builder);
                if (config.showUnbreakable()) appendUnbreakable(builder);
            }

            return builder.build();
        }
    }

}
