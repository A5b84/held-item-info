package io.github.a5b84.helditeminfo.config;

import io.github.a5b84.helditeminfo.HeldItemInfo;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.PrefixText;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;

import java.util.Collections;
import java.util.List;

import static io.github.a5b84.helditeminfo.Util.FONT_HEIGHT;

public class HeldItemInfoConfig {

    // General stuff

    public int maxLines() { return 6; }
    public int maxLineLength() { return 48; }
    public boolean respectHideFlags() { return true; }

    public int lineHeight() { return FONT_HEIGHT - 1; }
    public float offsetPerExtraLine() { return .3334f; }
    public int itemNameSpacing() { return 2; }
    public int verticalOffset() { return 0; }
    public float baseFadeDuration() { return 2; }
    public float fadeDurationPerExtraLine() { return 0.2f; }

    // Toggles

    public boolean showName() { return true; }
    public boolean showEnchantments() { return true; }
    public boolean showPotionEffects() { return true; }
    public boolean showContainerContent() { return true; }
    public boolean mergeSimilarContainerItems() { return true; }
    public boolean showFireworkAttributes() { return true; }
    public boolean showCommandBlockInfo() { return true; }
    public int maxCommandLines() { return 2; }
    public boolean showBeehiveContent() { return true; }
    public boolean showSpawnerEntity() { return true; }
    public boolean showCrossbowProjectiles() { return true; }
    public boolean showLore() { return true; }
    public int maxLoreLines() { return 3; }
    public boolean showUnbreakable() { return true; }
    public boolean showSignText() { return true; }
    public boolean showMusicDiscDescription() { return true; }
    public boolean showPaintingDescription() { return true; }
    public boolean showGoatHornInstrument() { return true; }
    public boolean showBookMeta() { return true; }
    public boolean showFilledMapId() { return false; }
    public boolean showEntityBucketContent() { return true; }
    public boolean showBlockState() { return true; }
    public boolean showHiddenLinesCount() { return true; }


    // Enchantments

    public boolean showOnlyFilteredEnchantments() { return false; }


    /** Config class usable by / requiring AutoConfig */
    @SuppressWarnings("FieldMayBeFinal")
    @Config(name = HeldItemInfo.MOD_ID)
    public static class HeldItemInfoAutoConfig extends HeldItemInfoConfig implements ConfigData {

        @BoundedDiscrete(min = 0, max = 40)
        @Tooltip private int maxLines = super.maxLines();
        @BoundedDiscrete(max = 240)
        @Tooltip(count = 2) private int maxLineLength = super.maxLineLength();
        @Tooltip private boolean respectHideFlags = super.respectHideFlags();

        @Tooltip private int lineHeight = super.lineHeight();
        @Tooltip(count = 2) private float offsetPerExtraLine = super.offsetPerExtraLine();
        @Tooltip private int itemNameSpacing = super.itemNameSpacing();
        private int verticalOffset = super.verticalOffset();
        @Tooltip private float baseFadeDuration = super.baseFadeDuration();
        @Tooltip private float fadeDurationPerExtraLine = super.fadeDurationPerExtraLine();

        @PrefixText
        private boolean showName = super.showName();
        private boolean showEnchantments = super.showEnchantments();
        private boolean showPotionEffects = super.showPotionEffects();
        private boolean showContainerContent = super.showContainerContent();
        private boolean mergeSimilarContainerItems = super.mergeSimilarContainerItems();
        private boolean showFireworkAttributes = super.showFireworkAttributes();
        private boolean showCommandBlockInfo = super.showCommandBlockInfo();
        @BoundedDiscrete(max = 40)
        @Tooltip private int maxCommandLines = super.maxCommandLines();
        private boolean showBeehiveContent = super.showBeehiveContent();
        private boolean showSpawnerEntity = super.showSpawnerEntity();
        private boolean showCrossbowProjectiles = super.showCrossbowProjectiles();
        private boolean showLore = super.showLore();
        @BoundedDiscrete(max = 40)
        private int maxLoreLines = super.maxLoreLines();
        private boolean showUnbreakable = super.showUnbreakable();
        private boolean showSignText = super.showSignText();
        private boolean showMusicDiscDescription = super.showMusicDiscDescription();
        private boolean showPaintingDescription = super.showPaintingDescription();
        private boolean showGoatHornInstrument = super.showGoatHornInstrument();
        private boolean showBookMeta = super.showBookMeta();
        private boolean showFilledMapId = super.showFilledMapId();
        private boolean showEntityBucketContent = super.showEntityBucketContent();
        @Tooltip private boolean showBlockState = super.showBlockState();
        private boolean showHiddenLinesCount = super.showHiddenLinesCount();

        @Category("enchantments")
        @Tooltip(count = 2) private boolean showOnlyFilteredEnchantments = super.showOnlyFilteredEnchantments();
        @Category("enchantments")
        @Tooltip(count = 2) private List<String> filteredEnchantments = Collections.emptyList();


        @Override public int maxLines() { return maxLines; }
        @Override public int maxLineLength() { return maxLineLength; }
        @Override public boolean respectHideFlags() { return respectHideFlags; }

        @Override public int lineHeight() { return lineHeight; }
        @Override public float offsetPerExtraLine() { return offsetPerExtraLine; }
        @Override public int itemNameSpacing() { return itemNameSpacing; }
        @Override public int verticalOffset() { return verticalOffset; }
        @Override public float baseFadeDuration() { return baseFadeDuration; }
        @Override public float fadeDurationPerExtraLine() { return fadeDurationPerExtraLine; }

        @Override public boolean showName() { return showName; }
        @Override public boolean showEnchantments() { return showEnchantments; }
        @Override public boolean showPotionEffects() { return showPotionEffects; }
        @Override public boolean showContainerContent() { return showContainerContent; }
        @Override public boolean mergeSimilarContainerItems() { return mergeSimilarContainerItems; }
        @Override public boolean showFireworkAttributes() { return showFireworkAttributes; }
        @Override public boolean showCommandBlockInfo() { return showCommandBlockInfo; }
        @Override public int maxCommandLines() { return maxCommandLines; }
        @Override public boolean showBeehiveContent() { return showBeehiveContent; }
        @Override public boolean showSpawnerEntity() { return showSpawnerEntity; }
        @Override public boolean showCrossbowProjectiles() { return showCrossbowProjectiles; }
        @Override public boolean showLore() { return showLore; }
        @Override public int maxLoreLines() { return maxLoreLines; }
        @Override public boolean showUnbreakable() { return showUnbreakable; }
        @Override public boolean showSignText() { return showSignText; }
        @Override public boolean showMusicDiscDescription() { return showMusicDiscDescription; }
        @Override public boolean showPaintingDescription() { return showPaintingDescription; }
        @Override public boolean showGoatHornInstrument() { return showGoatHornInstrument; }
        @Override public boolean showBookMeta() { return showBookMeta; }
        @Override public boolean showFilledMapId() { return showFilledMapId; }
        @Override public boolean showEntityBucketContent() { return showEntityBucketContent; }
        @Override public boolean showBlockState() { return showBlockState; }
        @Override public boolean showHiddenLinesCount() { return showHiddenLinesCount; }

        @Override public boolean showOnlyFilteredEnchantments() { return showOnlyFilteredEnchantments; }
        public List<String> filteredEnchantments() { return filteredEnchantments; }
    }

}
