package io.github.a5b84.helditeminfo.mixin;

import io.github.a5b84.helditeminfo.Appenders;
import io.github.a5b84.helditeminfo.ContainerContentAppender;
import io.github.a5b84.helditeminfo.HeldItemInfo;
import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import io.github.a5b84.helditeminfo.TooltipLine;
import io.github.a5b84.helditeminfo.Util;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
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

@Mixin(Gui.class)
public abstract class GuiMixin {

  /**
   * Value in 1.21.6: 59.
   *
   * @see Gui#renderSelectedItemName(GuiGraphics)
   */
  @SuppressWarnings("JavadocReference")
  @Unique
  private static final int VANILLA_TOOLTIP_Y_OFFSET =
      ContextualBarRenderer.MARGIN_BOTTOM // Bottom of experience bar to bottom of screen
          + ContextualBarRenderer.HEIGHT
          + 2 * GuiAccessor.getLineHeight()
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
  @Unique private ItemStack stackBeforeTick;

  /** Width of the longest line, or some negative number if not computed yet */
  @Unique private int maxWidth = -1;

  /**
   * Y coordinate of the top of the tooltip if it has been rendered this frame, some negative number
   * otherwise.
   */
  @Unique private int lastTooltipY;

  @Inject(method = "render", at = @At("HEAD"))
  private void onBeforeRender(CallbackInfo ci) {
    lastTooltipY = -1;
  }

  /** Replaces vanilla rendering with the mod's */
  @Redirect(
      method = "renderSelectedItemName",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/GuiGraphics;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V"))
  private void drawTextProxy(
      GuiGraphics graphics, Font font, Component text, int _x, int y, int width, int color) {
    int lineHeight = HeldItemInfo.config.lineHeight();

    y -=
        (int) ((lineHeight - HeldItemInfo.config.offsetPerExtraLine()) * (tooltip.size() - 1))
            + HeldItemInfo.config.verticalOffset();

    if (HeldItemInfo.config.showName() && tooltip.size() > 1) {
      y -= HeldItemInfo.config.itemNameSpacing();
    }

    //noinspection DataFlowIssue
    if (HeldItemInfo.config.preventOverlap() && minecraft.gameMode.canHurtPlayer()) {
      Player player = getCameraPlayer();
      if (player != null) {
        y -= getHealthBarsTotalHeight(player) - GuiAccessor.getLineHeight();
      }
    }

    lastTooltipY = y;

    drawBackground(graphics, y);

    int i = 0;
    for (TooltipLine line : tooltip) {
      int x = (graphics.guiWidth() - line.width) / 2;
      graphics.drawString(font, line.text, x, y, color);
      y += lineHeight;

      if (i == 0 && HeldItemInfo.config.showName()) {
        y += HeldItemInfo.config.itemNameSpacing();
      }
      i++;
    }
  }

  /**
   * @see Gui#renderPlayerHealth(GuiGraphics)
   */
  @SuppressWarnings("JavadocReference")
  @Unique
  private int getHealthBarsTotalHeight(Player player) {
    float totalHalfHearts =
        Math.max(
                (float) player.getAttributeValue(Attributes.MAX_HEALTH),
                Math.max(lastHealth, displayHealth))
            + Mth.ceil(player.getAbsorptionAmount());
    int rows = Mth.ceil(totalHalfHearts / 2 / GuiAccessor.getNumHeartsPerRow());
    int lineHeight = GuiAccessor.getLineHeight();
    int rowOffset = Math.max(lineHeight - (rows - 2), 3);
    return lineHeight + (rows - 1) * rowOffset;
  }

  @Unique
  private void drawBackground(GuiGraphics graphics, int y) {
    int backgroundColor = getBackgroundColor();

    if (ARGB.alpha(backgroundColor) != 0) {
      int scaledWidth = graphics.guiWidth();
      int height = HeldItemInfo.config.lineHeight() * tooltip.size();
      if (HeldItemInfo.config.showName() && tooltip.size() > 1) {
        height += HeldItemInfo.config.itemNameSpacing();
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
    return switch (HeldItemInfo.config.tooltipBackgroundVisibility()) {
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
      method = "renderOverlayMessage",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/gui/GuiGraphics;drawStringWithBackdrop(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V"))
  private void onDrawOverlayMessage(
      GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
    if (HeldItemInfo.config.preventOverlap() && lastTooltipY >= 0) {
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
        tooltip = TooltipLine.from(newInfo);
        maxWidth = -1;
        toolHighlightTimer =
            (int)
                (SharedConstants.TICKS_PER_SECOND
                    * (HeldItemInfo.config.baseFadeDuration()
                        + HeldItemInfo.config.fadeDurationPerExtraLine() * (tooltip.size() - 1)));
      }
    }
  }

  @Unique
  private List<Component> buildTooltip(ItemStack stack) {
    if (stack.isEmpty()) {
      return Collections.emptyList();
    } else {
      TooltipBuilder builder = new TooltipBuilder(stack);

      if (HeldItemInfo.config.showName()) {
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
        if (HeldItemInfo.config.showEntityBucketContent()) {
          builder.appendComponent(DataComponents.TROPICAL_FISH_PATTERN);
        }

        if (HeldItemInfo.config.showGoatHornInstrument()) {
          builder.appendComponent(DataComponents.INSTRUMENT);
        }

        if (HeldItemInfo.config.showFilledMapId()) {
          builder.appendComponent(DataComponents.MAP_ID);
        }

        if (HeldItemInfo.config.showBeehiveContent()) {
          builder.appendComponent(DataComponents.BEES);
        }

        if (HeldItemInfo.config.showContainerContent()) {
          ContainerContentAppender.appendContainerContent(builder);
        }

        if (HeldItemInfo.config.showBookMeta()) {
          builder.appendComponent(DataComponents.WRITTEN_BOOK_CONTENT);
        }

        if (HeldItemInfo.config.showCrossbowProjectiles()) {
          builder.appendComponent(DataComponents.CHARGED_PROJECTILES, Util::withDefaultColor);
        }

        if (HeldItemInfo.config.showFireworkAttributes()) {
          builder.appendComponent(DataComponents.FIREWORKS);
        }

        if (HeldItemInfo.config.showFireworkAttributes()) {
          builder.appendComponent(DataComponents.FIREWORK_EXPLOSION);
        }

        if (HeldItemInfo.config.showPotionEffects()) {
          Appenders.appendPotionEffects(builder);
        }

        if (HeldItemInfo.config.showMusicDiscDescription()) {
          Appenders.appendMusicDiscDescription(builder);
        }

        if (HeldItemInfo.config.showEnchantments()) {
          Appenders.appendEnchantments(builder);
        }

        if (HeldItemInfo.config.showLore()) {
          Appenders.appendLore(builder);
        }

        if (HeldItemInfo.config.showUnbreakable()) {
          Appenders.appendUnbreakable(builder);
        }

        if (HeldItemInfo.config.showPotionEffects()) {
          builder.appendComponent(DataComponents.OMINOUS_BOTTLE_AMPLIFIER);
        }

        if (HeldItemInfo.config.showBlockState()) {
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
