package io.github.a5b84.helditeminfo.mixin;

import io.github.a5b84.helditeminfo.Appenders;
import io.github.a5b84.helditeminfo.ContainerContentAppender;
import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import io.github.a5b84.helditeminfo.TooltipLine;
import io.github.a5b84.helditeminfo.Util;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.bar.Bar;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
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

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;
import static io.github.a5b84.helditeminfo.Util.FONT_HEIGHT;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    /**
     * Value in 1.21.6: 59.
     *
     * @see InGameHud#renderHeldItemTooltip(DrawContext)
     */
    @SuppressWarnings("JavadocReference")
    @Unique
    private static final int VANILLA_TOOLTIP_Y_OFFSET =
            Bar.VERTICAL_OFFSET // Bottom of experience bar to bottom of screen
                    + Bar.HEIGHT
                    + 2 * InGameHudAccessor.getLineHeight()
                    + 1 // Spacing between armor bar and item name
                    + FONT_HEIGHT;

    @Shadow @Final private MinecraftClient client;
    @Shadow private int heldItemTooltipFade;
    @Shadow private ItemStack currentStack;
    @Shadow private int lastHealthValue;
    @Shadow private int renderHealthValue;

    @Shadow @Nullable protected abstract PlayerEntity getCameraPlayer();

    @Unique
    private List<TooltipLine> tooltip = Collections.emptyList();
    @Unique
    private ItemStack stackBeforeTick;

    /**
     * Width of the longest line, or some negative number if not computed yet
     */
    @Unique
    private int maxWidth = -1;

    /**
     * Y coordinate of the top of the tooltip if it has been rendered this frame,
     * some negative number otherwise.
     */
    @Unique
    private int lastTooltipY;


    @Inject(method = "render", at = @At("HEAD"))
    private void onBeforeRender(CallbackInfo ci) {
        lastTooltipY = -1;
    }


    /**
     * Replaces vanilla rendering with the mod's
     */
    @Redirect(method = "renderHeldItemTooltip",
            at = @At(value = "INVOKE", target = "net/minecraft/client/gui/DrawContext.drawTextWithBackground(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIII)V"))
    private void drawTextProxy(DrawContext context, TextRenderer textRenderer, Text text, int _x, int y, int width, int color) {
        int lineHeight = config.lineHeight();

        y -= (int) ((lineHeight - config.offsetPerExtraLine()) * (tooltip.size() - 1))
                + config.verticalOffset();

        if (config.showName() && tooltip.size() > 1) {
            y -= config.itemNameSpacing();
        }

        //noinspection DataFlowIssue ("Method invocation 'hasStatusBars' may produce 'NullPointerException'")
        if (config.preventOverlap() && client.interactionManager.hasStatusBars()) {
            PlayerEntity player = getCameraPlayer();
            if (player != null) {
                y -= getHealthBarsTotalHeight(player) - InGameHudAccessor.getLineHeight();
            }
        }

        lastTooltipY = y;

        drawBackground(context, y);

        int i = 0;
        for (TooltipLine line : tooltip) {
            int x = (context.getScaledWindowWidth() - line.width) / 2;
            context.drawTextWithShadow(textRenderer, line.text, x, y, color);
            y += lineHeight;

            if (i == 0 && config.showName()) {
                y += config.itemNameSpacing();
            }
            i++;
        }
    }

    /**
     * @see InGameHud#renderStatusBars(DrawContext)
     */
    @SuppressWarnings("JavadocReference")
    @Unique
    private int getHealthBarsTotalHeight(PlayerEntity player) {
        float totalHalfHearts = Math.max(
                (float) player.getAttributeValue(EntityAttributes.MAX_HEALTH),
                Math.max(lastHealthValue, renderHealthValue)
        ) + MathHelper.ceil(player.getAbsorptionAmount());
        int rows = MathHelper.ceil(totalHalfHearts / 2 / InGameHudAccessor.getNumHeartsPerRow());
        int lineHeight = InGameHudAccessor.getLineHeight();
        int rowOffset = Math.max(
                lineHeight - (rows - 2),
                3
        );
        return lineHeight + (rows - 1) * rowOffset;
    }

    @Unique
    private void drawBackground(DrawContext context, int y) {
        int backgroundColor = getBackgroundColor();

        if (ColorHelper.getAlpha(backgroundColor) != 0) {
            int scaledWidth = context.getScaledWindowWidth();
            int height = config.lineHeight() * tooltip.size();
            if (config.showName() && tooltip.size() > 1) {
                height += config.itemNameSpacing();
            }
            int padding = 2;
            computeMaxWidth();

            context.fill(
                    (scaledWidth - maxWidth) / 2 - padding,
                    y - padding,
                    (scaledWidth + maxWidth) / 2 + padding,
                    y + height + padding,
                    backgroundColor);
        }
    }

    @Unique
    private int getBackgroundColor() {
        return switch (config.tooltipBackgroundVisibility()) {
            case VANILLA -> client.options.getTextBackgroundColor(0);
            case ALWAYS ->  ColorHelper.fromFloats(client.options.getTextBackgroundOpacity().getValue().floatValue(), 0, 0, 0);
            case NEVER -> 0;
        };
    }

    @Unique
    private void computeMaxWidth() {
        if (maxWidth < 0) {
            for (TooltipLine line : tooltip) {
                if (line.width > maxWidth) {
                    maxWidth = line.width;
                }
            }
        }
    }


    @Inject(method = "renderOverlayMessage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithBackground(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIII)V"))
    private void onDrawOverlayMessage(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (config.preventOverlap() && lastTooltipY >= 0) {
            int tooltipYOffset = context.getScaledWindowHeight() - lastTooltipY;
            int difference = VANILLA_TOOLTIP_Y_OFFSET - tooltipYOffset;
            if (difference < 0) {
                context.getMatrices().translate(0, difference);
            }
        }
    }


    @Inject(method = "tick()V", at = @At("HEAD"))
    private void onBeforeTick(CallbackInfo ci) {
        stackBeforeTick = currentStack;
    }

    /**
     * Rebuilds the tooltip
     */
    @Inject(method = "tick()V", at = @At("RETURN"))
    private void onAfterTick(CallbackInfo ci) {
        if (client.player == null || currentStack == stackBeforeTick) {
            return;
        }

        // New tooltip
        if (currentStack.isEmpty()) {
            tooltip = Collections.emptyList();
        } else {
            List<Text> newInfo = buildTooltip(currentStack);

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
            TooltipBuilder builder = new TooltipBuilder(stack);

            // Stack name
            if (config.showName()) {
                // Using Text.empty().append(...).formatted(...) because formatted(...) mutates the object (see ItemStack.getTooltip)
                MutableText stackName = Text.empty()
                        .append(stack.getName())
                        .formatted(stack.getRarity().getFormatting());
                if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
                    stackName.formatted(Formatting.ITALIC);
                }

                builder.append(stackName);
            }

            if (builder.shouldDisplayComponents()) {
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

                // Component-related lines
                if (config.showEntityBucketContent()) builder.appendComponent(DataComponentTypes.TROPICAL_FISH_PATTERN);
                if (config.showGoatHornInstrument()) builder.appendComponent(DataComponentTypes.INSTRUMENT);
                if (config.showFilledMapId()) builder.appendComponent(DataComponentTypes.MAP_ID);
                if (config.showBeehiveContent()) builder.appendComponent(DataComponentTypes.BEES);
                if (config.showContainerContent()) ContainerContentAppender.appendContainerContent(builder);
                if (config.showBookMeta()) builder.appendComponent(DataComponentTypes.WRITTEN_BOOK_CONTENT);
                if (config.showCrossbowProjectiles())
                    builder.appendComponent(DataComponentTypes.CHARGED_PROJECTILES, Util::withDefaultColor);
                if (config.showFireworkAttributes()) builder.appendComponent(DataComponentTypes.FIREWORKS);
                if (config.showFireworkAttributes()) builder.appendComponent(DataComponentTypes.FIREWORK_EXPLOSION);
                if (config.showPotionEffects()) Appenders.appendPotionEffects(builder);
                if (config.showMusicDiscDescription()) Appenders.appendMusicDiscDescription(builder);
                if (config.showEnchantments()) Appenders.appendEnchantments(builder);
                if (config.showLore()) Appenders.appendLore(builder);
                if (config.showUnbreakable()) Appenders.appendUnbreakable(builder);
                if (config.showPotionEffects()) builder.appendComponent(DataComponentTypes.OMINOUS_BOTTLE_AMPLIFIER);
                if (config.showBlockState()) builder.appendComponent(DataComponentTypes.BLOCK_STATE);
            }

            return builder.build();
        }
    }
}
