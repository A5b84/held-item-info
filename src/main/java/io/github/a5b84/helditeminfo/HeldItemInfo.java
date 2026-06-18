package io.github.a5b84.helditeminfo;

import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig;
import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig.HeldItemInfoAutoConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.IdentifierException;
import net.minecraft.resources.Identifier;
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

  private static final List<Identifier> filteredEnchantments = new ArrayList<>();

  @Override
  public void onInitializeClient() {
    if (USE_CLOTH_CONFIG) {
      ConfigHolder<HeldItemInfoAutoConfig> holder =
          AutoConfig.register(HeldItemInfoAutoConfig.class, GsonConfigSerializer::new);
      config = holder.getConfig();

      holder.registerSaveListener(
          (_, config) -> {
            updateFilteredEnchantments(config);
            return InteractionResult.SUCCESS;
          });

      holder.registerLoadListener(
          (_, config) -> {
            updateFilteredEnchantments(config);
            return InteractionResult.SUCCESS;
          });

      updateFilteredEnchantments(holder.getConfig());
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

  public static List<Identifier> getFilteredEnchantments() {
    return filteredEnchantments;
  }

  private static void updateFilteredEnchantments(HeldItemInfoAutoConfig config) {
    filteredEnchantments.clear();

    for (String id : config.filteredEnchantments()) {
      try {
        filteredEnchantments.add(Identifier.parse(id));
      } catch (IdentifierException e) {
        LOGGER.error("[Held Item Info] Invalid enchantment identifier '{}'", id, e);
      }
    }
  }
}
