package io.github.a5b84.helditeminfo.config;

import io.github.a5b84.helditeminfo.Mod;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.PrefixText;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static io.github.a5b84.helditeminfo.Mod.FONT_HEIGHT;

public class HeldItemInfoConfig {

    // General stuff

    public int maxLines() { return 6; }
    public int maxLineLength() { return 48; }
    public boolean respectHideFlags() { return true; }

    public int lineHeight() { return FONT_HEIGHT - 1; }
    public float offsetPerExtraLine() { return .3334f; }
    public float baseFadeDuration() { return 2; }
    public float fadeDurationPerExtraLine() { return 0.2f; }

    public boolean showName() { return true; }
    public boolean showBeehiveContent() { return true; }
    public boolean showCommandBlockInfo() { return true; }
    public int maxCommandLines() { return 2; }
    public boolean showPotionEffects() { return true; }
    public boolean showSignText() { return true; }
    public boolean showPatternName() { return true; }
    public boolean showFireworkEffects() { return true; }
    public boolean showFishInBucket() { return true; }
    public boolean showMusicDiscDescription() { return true; }
    public boolean showBookMeta() { return true; }
    public boolean showContainerContent() { return true; }
    public boolean showEnchantments() { return true; }
    public boolean showLore() { return true; }
    public int maxLoreLines() { return 3; }
    public boolean showUnbreakable() { return true; }



    // Enchants

    public boolean showOnlyFilteredEnchants() { return false; }
    public List<Identifier> filteredEnchants() { return Collections.emptyList(); }



    /** Config class usable by / requiring AutoConfig */
    @SuppressWarnings("FieldMayBeFinal")
    @Config(name = Mod.ID)
    public static class HeldItemInfoAutoConfig extends HeldItemInfoConfig implements ConfigData {

        @BoundedDiscrete(max = 40)
        @Tooltip private int maxLines = super.maxLines();
        @BoundedDiscrete(max = 240)
        @Tooltip(count = 2) private int maxLineLength = super.maxLineLength();
        @Tooltip private boolean respectHideFlags = super.respectHideFlags();

        @Tooltip private int lineHeight = super.lineHeight();
        @Tooltip(count = 2) private float offsetPerExtraLine = super.offsetPerExtraLine();
        @Tooltip private float baseFadeDuration = super.baseFadeDuration();
        @Tooltip private float fadeDurationPerExtraLine = super.fadeDurationPerExtraLine();

        @PrefixText
        private boolean showName = super.showName();
        private boolean showBeehiveContent = super.showBeehiveContent();
        private boolean showCommandBlockInfo = super.showCommandBlockInfo();
        @BoundedDiscrete(max = 40)
        @Tooltip private int maxCommandLines = super.maxCommandLines();
        private boolean showPotionEffects = super.showPotionEffects();
        private boolean showSignText = super.showSignText();
        @Tooltip private boolean showPatternName = super.showPatternName();
        private boolean showFireworkEffects = super.showFireworkEffects();
        private boolean showFishInBucket = super.showFishInBucket();
        private boolean showMusicDiscDescription = super.showMusicDiscDescription();
        private boolean showBookMeta = super.showBookMeta();
        private boolean showContainerContent = super.showContainerContent();
        private boolean showEnchantments = super.showEnchantments();
        private boolean showLore = super.showLore();
        private int maxLoreLines = super.maxLoreLines();
        private boolean showUnbreakable = super.showUnbreakable();

        @Category("enchants")
        @Tooltip private boolean showOnlyFilteredEnchants = super.showOnlyFilteredEnchants();
        @Category("enchants")
        @Tooltip(count = 2) private List<String> filteredEnchants = Collections.emptyList();



        @Override public int maxLines() { return maxLines; }
        @Override public int maxLineLength() { return maxLineLength; }
        @Override public boolean respectHideFlags() { return respectHideFlags; }

        @Override public int lineHeight() { return lineHeight; }
        @Override public float offsetPerExtraLine() { return offsetPerExtraLine; }
        @Override public float baseFadeDuration() { return baseFadeDuration; }
        @Override public float fadeDurationPerExtraLine() { return fadeDurationPerExtraLine; }

        @Override public boolean showName() { return showName; }
        @Override public boolean showBeehiveContent() { return showBeehiveContent; }
        @Override public boolean showCommandBlockInfo() { return showCommandBlockInfo; }
        @Override public int maxCommandLines() { return maxCommandLines; }
        @Override public boolean showPotionEffects() { return showPotionEffects; }
        @Override public boolean showSignText() { return showSignText; }
        @Override public boolean showPatternName() { return showPatternName; }
        @Override public boolean showFireworkEffects() { return showFireworkEffects; }
        @Override public boolean showFishInBucket() { return showFishInBucket; }
        @Override public boolean showMusicDiscDescription() { return showMusicDiscDescription; }
        @Override public boolean showBookMeta() { return showBookMeta; }
        @Override public boolean showContainerContent() { return showContainerContent; }
        @Override public boolean showEnchantments() { return showEnchantments; }
        @Override public boolean showLore() { return showLore; }
        @Override public int maxLoreLines() { return maxLoreLines; }
        @Override public boolean showUnbreakable() { return showUnbreakable; }

        public boolean showOnlyFilteredEnchants() { return showOnlyFilteredEnchants; }

        public List<Identifier> filteredEnchants() {
            List<Identifier> result = new LinkedList<>();
            for (String enchant : filteredEnchants) {
                try {
                    result.add(new Identifier(enchant));
                } catch (Throwable ignored) {}
            }
            return result;
        }

    }

}
