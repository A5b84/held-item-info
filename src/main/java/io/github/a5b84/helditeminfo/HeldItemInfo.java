package io.github.a5b84.helditeminfo;

import io.github.a5b84.helditeminfo.config.ConfigChangeListener;
import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig;
import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig.HeldItemInfoAutoConfig;
import java.util.Collections;
import java.util.List;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeldItemInfo implements ClientModInitializer {

  public static final String MOD_ID = "held-item-info";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
  public static final boolean USE_CLOTH_CONFIG =
      FabricLoader.getInstance().isModLoaded("cloth-config2");

  public static HeldItemInfoConfig config;
  public static List<Identifier> filteredEnchantments;

  @Override
  public void onInitializeClient() {
    if (USE_CLOTH_CONFIG) {
      ConfigHolder<HeldItemInfoAutoConfig> holder =
          AutoConfig.register(HeldItemInfoAutoConfig.class, GsonConfigSerializer::new);
      config = holder.getConfig();
      ConfigChangeListener changeListener = new ConfigChangeListener();
      changeListener.listen(holder);
      changeListener.onChange(holder, holder.getConfig());
    } else {
      config = new HeldItemInfoConfig();
      filteredEnchantments = Collections.emptyList();
    }

    if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
      ClientCommandRegistrationCallback.EVENT.register(
          (dispatcher, registryAccess) -> HeldItemInfoDebugCommand.register(dispatcher));
    }
  }
}
