package io.github.a5b84.helditeminfo.config;

import io.github.a5b84.helditeminfo.HeldItemInfo;
import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig.HeldItemInfoAutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import java.util.ArrayList;
import java.util.List;

import static io.github.a5b84.helditeminfo.HeldItemInfo.LOGGER;

public class ConfigChangeListener {

    public void listen(ConfigHolder<HeldItemInfoAutoConfig> holder) {
        holder.registerSaveListener(this::onChange);

        holder.registerLoadListener(this::onChange);
    }

    public ActionResult onChange(ConfigHolder<HeldItemInfoAutoConfig> holder, HeldItemInfoAutoConfig config) {
        // Recreate enchantment filters
        List<String> idStrings = config.filteredEnchantments();
        List<Identifier> ids = new ArrayList<>(idStrings.size());
        for (String idString : idStrings) {
            try {
                ids.add(Identifier.of(idString));
            } catch (InvalidIdentifierException e) {
                LOGGER.error("[Held Item Info] Invalid enchantment identifier '" + idString + "': " + e.getMessage());
            }
        }
        HeldItemInfo.filteredEnchantments = ids;

        return ActionResult.SUCCESS;
    }

}
