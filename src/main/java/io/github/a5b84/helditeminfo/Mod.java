package io.github.a5b84.helditeminfo;

import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig;
import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig.HeldItemInfoAutoConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class Mod implements ClientModInitializer {

    public static final String ID = "held-item-info";
    public static final boolean USE_CLOTH_CONFIG = FabricLoader.getInstance().isModLoaded("cloth-config2");
    public static final int FONT_HEIGHT = 9;

    public static HeldItemInfoConfig config;


    @Override
    public void onInitializeClient() {
        if (USE_CLOTH_CONFIG) {
            AutoConfig.register(HeldItemInfoAutoConfig.class, GsonConfigSerializer::new);
            config = AutoConfig.getConfigHolder(HeldItemInfoAutoConfig.class).getConfig();
        } else {
            config = new HeldItemInfoConfig();
        }
    }

}
