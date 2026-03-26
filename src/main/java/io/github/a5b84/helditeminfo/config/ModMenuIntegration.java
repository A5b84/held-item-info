package io.github.a5b84.helditeminfo.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.a5b84.helditeminfo.HeldItemInfo;
import me.shedaniel.autoconfig.AutoConfigClient;

public class ModMenuIntegration implements ModMenuApi {

  private static final ConfigScreenFactory<?> FACTORY =
      HeldItemInfo.USE_CLOTH_CONFIG
          ? parent ->
              AutoConfigClient.getConfigScreen(
                      HeldItemInfoConfig.HeldItemInfoAutoConfig.class, parent)
                  .get()
          : _ -> null;

  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return FACTORY;
  }
}
