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
import net.minecraft.component.DataComponentTypes;
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

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;
import static io.github.a5b84.helditeminfo.Util.FONT_HEIGHT;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow private @Final MinecraftClient client;
    @Shadow private int heldItemTooltipFade;
    @Shadow private ItemStack currentStack;

    @Unique private List<TooltipLine> tooltip = Collections.emptyList();
    @Unique private int y;
    @Unique private ItemStack stackBeforeTick;

    /** Width of the longest line, or some negative number if not computed yet */
    @Unique private int maxWidth = -1;


    /** Updates rendering variables */
    @Inject(method = "renderHeldItemTooltip",
            at = @At(value = "INVOKE", target = "net/minecraft/client/font/TextRenderer.getWidth(Lnet/minecraft/text/StringVisitable;)I"))
    public void onBeforeRenderHeldItemTooltip(DrawContext context, CallbackInfo ci) {
        y = context.getScaledWindowHeight()
                - Bar.VERTICAL_OFFSET // Bottom of experience bar to bottom of screen
                - Bar.HEIGHT
                - 2 * InGameHudAccessor.getLineHeight()
                - 1 // Spacing between armor bar and item name
                - FONT_HEIGHT
                - (int) ((config.lineHeight() - config.offsetPerExtraLine()) * (tooltip.size() - 1))
                - config.verticalOffset();

        //noinspection ConstantConditions
        if (!this.client.interactionManager.hasStatusBars()) {
            y += 14; // See InGameHud#renderHeldItemTooltip
        }

        if (config.showName() && tooltip.size() > 1) {
            y -= config.itemNameSpacing();
        }
    }


    /** Replaces vanilla rendering with the mod's */
    @Redirect(method = "renderHeldItemTooltip",
            at = @At(value = "INVOKE", target = "net/minecraft/client/gui/DrawContext.drawTextWithBackground(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIII)V"))
    private void drawTextProxy(DrawContext context, TextRenderer textRenderer, Text text, int _x, int _y, int width, int color) {
        int backgroundColor = client.options.getTextBackgroundColor(0);

        if ((backgroundColor & 0xff000000) != 0) {
            if (maxWidth < 0) {
                for (TooltipLine line : tooltip) {
                    if (line.width > maxWidth) {
                        maxWidth = line.width;
                    }
                }
            }

            int scaledWidth = context.getScaledWindowWidth();
            int height = config.lineHeight() * tooltip.size();
            if (config.showName() && tooltip.size() > 1) {
                height += config.itemNameSpacing();
            }

            context.fill(
                    (scaledWidth - maxWidth) / 2 - 2, y - 2,
                    (scaledWidth + maxWidth) / 2 + 2, y + height + 2,
                    backgroundColor);
        }

        int lineHeight = config.lineHeight();
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
                if (config.showCrossbowProjectiles()) builder.appendComponent(DataComponentTypes.CHARGED_PROJECTILES, Util::withDefaultColor);
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
