package io.github.a5b84.helditeminfo;

public interface TooltipAppender {

  /**
   * @return {@code true} if the tooltip from {@link #heldItemInfo_appendTooltip} should be
   *     displayed, {@code false} otherwise.
   */
  boolean heldItemInfo_shouldAppendTooltip();

  void heldItemInfo_appendTooltip(TooltipBuilder builder);
}
