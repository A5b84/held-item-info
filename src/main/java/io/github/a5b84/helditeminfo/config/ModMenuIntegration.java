package io.github.a5b84.helditeminfo.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.a5b84.helditeminfo.HeldItemInfo;
import me.shedaniel.autoconfig.AutoConfig;

public class ModMenuIntegration implements ModMenuApi {

    private static final ConfigScreenFactory<?> FACTORY = HeldItemInfo.USE_CLOTH_CONFIG
            ? parent -> AutoConfig.getConfigScreen(HeldItemInfoConfig.HeldItemInfoAutoConfig.class, parent).get()
            : parent -> null;

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return FACTORY;
    }

}
