package io.github.a5b84.helditeminfo;

import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig;
import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig.HeldItemInfoAutoConfig;
import java.util.Objects;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.InteractionResult;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeldItemInfo implements ClientModInitializer {

  public static final String MOD_ID = "held-item-info";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
  public static final boolean USE_CLOTH_CONFIG =
      FabricLoader.getInstance().isModLoaded("cloth-config2");

  @Nullable private static HeldItemInfoConfig config;

  @Override
  public void onInitializeClient() {
    if (USE_CLOTH_CONFIG) {
      ConfigHolder<HeldItemInfoAutoConfig> holder =
          AutoConfig.register(HeldItemInfoAutoConfig.class, GsonConfigSerializer::new);
      HeldItemInfoAutoConfig autoConfig = holder.getConfig();
      autoConfig.onSavedOrLoaded();
      config = autoConfig;

      holder.registerSaveListener((_, config) -> {
        config.onSavedOrLoaded();
        return InteractionResult.SUCCESS;
      });

      holder.registerLoadListener((_, config) -> {
        config.onSavedOrLoaded();
        return InteractionResult.SUCCESS;
      });
    } else {
      config = new HeldItemInfoConfig();
    }

    if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
      ClientCommandRegistrationCallback.EVENT.register(
          (dispatcher, _) -> HeldItemInfoDebugCommand.register(dispatcher));
    }
  }

  public static HeldItemInfoConfig getConfig() {
    return Objects.requireNonNull(config);
  }
}
