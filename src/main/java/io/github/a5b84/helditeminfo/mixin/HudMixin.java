package io.github.a5b84.helditeminfo.mixin;

import io.github.a5b84.helditeminfo.Appenders;
import io.github.a5b84.helditeminfo.HeldItemInfo;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import io.github.a5b84.helditeminfo.TooltipLine;
import io.github.a5b84.helditeminfo.Util;
import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig;
import java.util.Collections;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {

  /**
   * Value in 1.21.6: 59.
   *
   * @see Hud#extractSelectedItemName(GuiGraphicsExtractor)
   */
  @SuppressWarnings("JavadocReference")
  @Unique
  private static final int VANILLA_TOOLTIP_Y_OFFSET =
      ContextualBar.MARGIN_BOTTOM // Bottom of experience bar to bottom of screen
          + ContextualBar.HEIGHT
          + 2 * HudAccessor.getLineHeight()
          + 1 // Spacing between armor bar and item name
          + Util.FONT_HEIGHT;

  @Unique private final HeldItemInfoConfig config = HeldItemInfo.getConfig();

  @Shadow @Final private Minecraft minecraft;
  @Shadow private int toolHighlightTimer;
  @Shadow private ItemStack lastToolHighlight;
  @Shadow private int lastHealth;
  @Shadow private int displayHealth;

  @Shadow
  @Nullable
  protected abstract Player getCameraPlayer();

  @Unique private List<TooltipLine> tooltip = Collections.emptyList();
  @Unique @Nullable private ItemStack stackBeforeTick;

  /** Width of the longest line, or some negative number if not computed yet */
  @Unique private int maxWidth = -1;

  /**
   * Y coordinate of the top of the tooltip if it has been rendered this frame, some negative number
   * otherwise.
   */
  @Unique private int lastTooltipY;

  @Inject(method = "extractRenderState", at = @At("HEAD"))
  private void beforeExtractRenderState(CallbackInfo ci) {
    lastTooltipY = -1;
  }

  /** Replaces vanilla rendering with the mod's */
  @Redirect(
      method = "extractSelectedItemName",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/GuiGraphicsExtractor;textWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V"))
  private void drawTextProxy(
      GuiGraphicsExtractor graphics,
      Font font,
      Component str,
      int textX,
      int textY,
      int textWidth,
      int textColor) {
    int lineHeight = config.lineHeight();

    textY -=
        (int) ((lineHeight - config.offsetPerExtraLine()) * (tooltip.size() - 1))
            + config.verticalOffset();

    if (config.showName() && tooltip.size() > 1) {
      textY -= config.itemNameSpacing();
    }

    //noinspection DataFlowIssue
    if (config.preventOverlap() && minecraft.gameMode.canHurtPlayer()) {
      Player player = getCameraPlayer();
      if (player != null) {
        textY -= getHealthBarsTotalHeight(player) - HudAccessor.getLineHeight();
      }
    }

    lastTooltipY = textY;

    drawBackground(graphics, textY);

    int i = 0;
    for (TooltipLine line : tooltip) {
      int x = (graphics.guiWidth() - line.width) / 2;
      graphics.text(font, line.text, x, textY, textColor);
      textY += lineHeight;

      if (i == 0 && config.showName()) {
        textY += config.itemNameSpacing();
      }
      i++;
    }
  }

  /**
   * @see Hud#extractPlayerHealth(GuiGraphicsExtractor)
   */
  @SuppressWarnings("JavadocReference")
  @Unique
  private int getHealthBarsTotalHeight(Player player) {
    float totalHalfHearts =
        Math.max(
                (float) player.getAttributeValue(Attributes.MAX_HEALTH),
                Math.max(lastHealth, displayHealth))
            + Mth.ceil(player.getAbsorptionAmount());
    int rows = Mth.ceil(totalHalfHearts / 2 / HudAccessor.getNumHeartsPerRow());
    int lineHeight = HudAccessor.getLineHeight();
    int rowOffset = Math.max(lineHeight - (rows - 2), 3);
    return lineHeight + (rows - 1) * rowOffset;
  }

  @Unique
  private void drawBackground(GuiGraphicsExtractor graphics, int y) {
    int backgroundColor = getBackgroundColor();

    if (ARGB.alpha(backgroundColor) != 0) {
      int scaledWidth = graphics.guiWidth();
      int height = config.lineHeight() * tooltip.size();
      if (config.showName() && tooltip.size() > 1) {
        height += config.itemNameSpacing();
      }
      int padding = 2;
      computeMaxWidth();

      graphics.fill(
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
      case VANILLA -> minecraft.options.getBackgroundColor(0);
      case ALWAYS ->
          ARGB.colorFromFloat(
              minecraft.options.textBackgroundOpacity().get().floatValue(), 0, 0, 0);
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

  @Inject(
      method = "extractOverlayMessage",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/GuiGraphicsExtractor;textWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V"))
  private void onExtractOverlayMessage(
      GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
    if (config.preventOverlap() && lastTooltipY >= 0) {
      int tooltipYOffset = graphics.guiHeight() - lastTooltipY;
      int difference = VANILLA_TOOLTIP_Y_OFFSET - tooltipYOffset;
      if (difference < 0) {
        graphics.pose().translate(0, difference);
      }
    }
  }

  @Inject(method = "tick()V", at = @At("HEAD"))
  private void onBeforeTick(CallbackInfo ci) {
    stackBeforeTick = lastToolHighlight;
  }

  /** Rebuilds the tooltip */
  @Inject(method = "tick()V", at = @At("RETURN"))
  private void onAfterTick(CallbackInfo ci) {
    if (minecraft.player == null || lastToolHighlight == stackBeforeTick) {
      return;
    }

    // New tooltip
    if (lastToolHighlight.isEmpty()) {
      tooltip = Collections.emptyList();
    } else {
      TooltipBuilder builder =
          new TooltipBuilder(
              lastToolHighlight,
              config,
              Item.TooltipContext.of(minecraft.level),
              minecraft.font.getSplitter(),
              minecraft.isTextFilteringEnabled());
      Appenders.appendAll(builder);
      List<Component> newInfo = builder.build();

      if (!TooltipLine.areEquivalent(tooltip, newInfo)) {
        tooltip = TooltipLine.from(newInfo, minecraft.font);
        maxWidth = -1;
        toolHighlightTimer =
            (int)
                (SharedConstants.TICKS_PER_SECOND
                    * (config.baseFadeDuration()
                        + config.fadeDurationPerExtraLine() * (tooltip.size() - 1)));
      }
    }
  }
}
