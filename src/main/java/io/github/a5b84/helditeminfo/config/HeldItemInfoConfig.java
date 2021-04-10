package io.github.a5b84.helditeminfo.config;

import io.github.a5b84.helditeminfo.Mod;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;

import static io.github.a5b84.helditeminfo.Mod.FONT_HEIGHT;

public class HeldItemInfoConfig {

    // Content stuff

    public int maxLines() { return 6; }
    public int maxCommandLines() { return 2; }
    public int maxCommandLineLength() { return 32; }
    public boolean respectHideFlags() { return true; }



    // Layout stuff

    public int lineHeight() { return FONT_HEIGHT - 1; }
    public float offsetPerExtraLine() { return .3334f; }
    public float baseFadeDuration() { return 2; }
    public float fadeDurationPerExtraLine() { return 0.2f; }



    /** Config class usable by / requiring AutoConfig */
    @SuppressWarnings("FieldMayBeFinal")
    @Config(name = Mod.ID)
    public static class HeldItemInfoAutoConfig extends HeldItemInfoConfig implements ConfigData {

        @Tooltip private int maxLines = super.maxLines();
        @Tooltip private int maxCommandLines = super.maxCommandLines();
        @Tooltip private int maxCommandLineLength = super.maxCommandLineLength();
        @Tooltip private boolean respectHideFlags = super.respectHideFlags();
        @Tooltip private int lineHeight = super.lineHeight();
        @Tooltip(count = 2) private float offsetPerExtraLine = super.offsetPerExtraLine();
        @Tooltip private float baseFadeDuration = super.baseFadeDuration();
        @Tooltip private float fadeDurationPerExtraLine = super.fadeDurationPerExtraLine();

        @Override public int maxLines() { return maxLines; }
        @Override public int maxCommandLines() { return maxCommandLines; }
        @Override public int maxCommandLineLength() { return maxCommandLineLength; }
        @Override public boolean respectHideFlags() { return respectHideFlags; }
        @Override public int lineHeight() { return lineHeight; }
        @Override public float offsetPerExtraLine() { return offsetPerExtraLine; }
        @Override public float baseFadeDuration() { return baseFadeDuration; }
        @Override public float fadeDurationPerExtraLine() { return fadeDurationPerExtraLine; }

    }

}
