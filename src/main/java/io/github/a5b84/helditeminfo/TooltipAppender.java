package io.github.a5b84.helditeminfo;

import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig;

public interface TooltipAppender {

  /**
   * @return {@code true} if the tooltip from {@link #heldItemInfo_appendTooltip} should be
   *     displayed, {@code false} otherwise.
   */
  boolean heldItemInfo_shouldAppendTooltip(HeldItemInfoConfig config);

  void heldItemInfo_appendTooltip(TooltipBuilder builder);
}
