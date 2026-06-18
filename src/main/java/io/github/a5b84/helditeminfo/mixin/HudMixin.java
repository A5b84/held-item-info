package io.github.a5b84.helditeminfo.mixin;

import io.github.a5b84.helditeminfo.Appenders;
import io.github.a5b84.helditeminfo.ContainerContentAppender;
import io.github.a5b84.helditeminfo.HeldItemInfo;
import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import io.github.a5b84.helditeminfo.TooltipLine;
import io.github.a5b84.helditeminfo.Util;
import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
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
    HeldItemInfoConfig config = HeldItemInfo.getConfig();
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
      HeldItemInfoConfig config = HeldItemInfo.getConfig();
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
    return switch (HeldItemInfo.getConfig().tooltipBackgroundVisibility()) {
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
    if (HeldItemInfo.getConfig().preventOverlap() && lastTooltipY >= 0) {
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
      List<Component> newInfo = buildTooltip(lastToolHighlight);

      if (!TooltipLine.areEquivalent(tooltip, newInfo)) {
        HeldItemInfoConfig config = HeldItemInfo.getConfig();
        tooltip = TooltipLine.from(newInfo);
        maxWidth = -1;
        toolHighlightTimer =
            (int)
                (SharedConstants.TICKS_PER_SECOND
                    * (config.baseFadeDuration()
                        + config.fadeDurationPerExtraLine() * (tooltip.size() - 1)));
      }
    }
  }

  @Unique
  private List<Component> buildTooltip(ItemStack stack) {
    if (stack.isEmpty()) {
      return Collections.emptyList();
    } else {
      HeldItemInfoConfig config = HeldItemInfo.getConfig();
      TooltipBuilder builder = new TooltipBuilder(stack);

      if (config.showName()) {
        appendStackName(stack, builder);
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
        if (config.showEntityBucketContent()) {
          builder.appendComponent(DataComponents.TROPICAL_FISH_PATTERN);
        }

        if (config.showGoatHornInstrument()) {
          builder.appendComponent(DataComponents.INSTRUMENT);
        }

        if (config.showFilledMapId()) {
          builder.appendComponent(DataComponents.MAP_ID);
        }

        if (config.showBeehiveContent()) {
          builder.appendComponent(DataComponents.BEES);
        }

        if (config.showContainerContent()) {
          ContainerContentAppender.appendContainerContent(builder);
        }

        if (config.showBookMeta()) {
          builder.appendComponent(DataComponents.WRITTEN_BOOK_CONTENT);
        }

        if (config.showCrossbowProjectiles()) {
          HeldItemInfo.getIntangibleProjectileVisibility().pushHidden();
          builder.appendComponent(DataComponents.CHARGED_PROJECTILES, Util::withDefaultColor);
          HeldItemInfo.getIntangibleProjectileVisibility().popHidden();
        }

        if (config.showFireworkAttributes()) {
          builder.appendComponent(DataComponents.FIREWORKS);
        }

        if (config.showFireworkAttributes()) {
          builder.appendComponent(DataComponents.FIREWORK_EXPLOSION);
        }

        if (config.showPotionEffects()) {
          Appenders.appendPotionEffects(builder);
        }

        if (config.showMusicDiscDescription()) {
          Appenders.appendMusicDiscDescription(builder);
        }

        if (config.showEnchantments()) {
          Appenders.appendEnchantments(builder);
        }

        if (config.showLore()) {
          Appenders.appendLore(builder);
        }

        if (config.showUnbreakable()) {
          Appenders.appendUnbreakable(builder);
        }

        if (config.showPotionEffects()) {
          builder.appendComponent(DataComponents.OMINOUS_BOTTLE_AMPLIFIER);
        }

        if (config.showBlockState()) {
          builder.appendComponent(DataComponents.BLOCK_STATE);
        }
      }

      return builder.build();
    }
  }

  @Unique
  private void appendStackName(ItemStack stack, TooltipBuilder builder) {
    // Using Component.empty().append(...).withStyle(...) because withStyle(...) mutates the object
    // (see ItemStack.getStyledHoverName)
    MutableComponent stackName =
        Component.empty().append(stack.getHoverName()).withStyle(stack.getRarity().color());
    if (stack.has(DataComponents.CUSTOM_NAME)) {
      stackName.withStyle(ChatFormatting.ITALIC);
    }

    builder.append(stackName);
  }
}
