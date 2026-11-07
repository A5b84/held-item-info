package io.github.a5b84.helditeminfo;

import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig;
import org.jspecify.annotations.Nullable;

public class HeldItemTooltipContext {

  /**
   * Context for the held item tooltip currently being built. {@code null} when not building a held
   * item tooltip (including when vanilla inventory tooltips are being built).
   *
   * <p>Avoid using this wherever possible to minimize reliance on global state.
   */
  @Nullable public static HeldItemTooltipContext instance;

  public final HeldItemInfoConfig config;
  public boolean hideIntangibleTooltip = false;
  public boolean filterEnchantments = false;

  public HeldItemTooltipContext(HeldItemInfoConfig config) {
    this.config = config;
  }
}
