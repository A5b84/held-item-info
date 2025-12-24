package io.github.a5b84.helditeminfo.config;

import static io.github.a5b84.helditeminfo.HeldItemInfo.LOGGER;

import io.github.a5b84.helditeminfo.HeldItemInfo;
import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig.HeldItemInfoAutoConfig;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;

public class ConfigChangeListener {

  public void listen(ConfigHolder<HeldItemInfoAutoConfig> holder) {
    holder.registerSaveListener(this::onChange);
    holder.registerLoadListener(this::onChange);
  }

  public InteractionResult onChange(
      ConfigHolder<HeldItemInfoAutoConfig> holder, HeldItemInfoAutoConfig config) {
    // Recreate enchantment filters
    List<String> idStrings = config.filteredEnchantments();
    List<ResourceLocation> ids = new ArrayList<>(idStrings.size());
    for (String idString : idStrings) {
      try {
        ids.add(ResourceLocation.parse(idString));
      } catch (ResourceLocationException e) {
        LOGGER.error("[Held Item Info] Invalid enchantment identifier '" + idString + "'", e);
      }
    }
    HeldItemInfo.filteredEnchantments = ids;

    return InteractionResult.SUCCESS;
  }
}
