package io.github.a5b84.helditeminfo.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.a5b84.helditeminfo.ItemInfo;
import io.github.a5b84.helditeminfo.ItemInfo.InfoLine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * Mixin qui affiche plus d'infos sur l'item tenu
 */
@Mixin(InGameHud.class)
public abstract class HeldItemTooltipMixin extends DrawableHelper {

    // Trucs de InGameHud
    @Shadow private @Final MinecraftClient client;
    @Shadow private int heldItemTooltipFade;
    @Shadow private ItemStack currentStack;
    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;

    /** @see  */
    @Shadow
    public TextRenderer getFontRenderer() { return null; }

    private static final int FONT_HEIGHT = 9;
    private static final int LINE_HEIGHT = FONT_HEIGHT - 1;
    private static final float OFFSET_PER_EXTRA_LINE = LINE_HEIGHT - .334f; // Décalage par ligne en plus



    private List<InfoLine> info = new ArrayList<>(0);

    private int y;

    /** Largeur de la ligne la plus large (pour l'arrière-plan),
     * ou `Integer.MIN_VALUE` si pas encore calculée */
    private int maxWidth = Integer.MIN_VALUE;

    private ItemStack stackBeforeTick;



    /** Met à jour les variables utilisées pour l'affichage.
     * @see InGameHud#renderHeldItemTooltip */
    @Inject(
        method = "renderHeldItemTooltip",
        at = @At(value = "INVOKE", target = "net/minecraft/text/Text.asFormattedString()Ljava/lang/String;")
        //      Pas HEAD pour faire des trucs que si ça va afficher
    )
    public void onBeforeRenderHeldItemTooltip(CallbackInfo ci) {
        // 50 = 32 (hotbar) + 14 (vie + xp) + 4 (espaces) (je crois)
        y = scaledHeight - 50 - FONT_HEIGHT - (int) (OFFSET_PER_EXTRA_LINE * (info.size() - 1));
        if (!this.client.interactionManager.hasStatusBars()) y += 14;
    }



    /** Affiche l'arrière-plan (si activé) avec la bonne taille.
     * @see InGameHud#renderHeldItemTooltip */
    @Redirect(
        method = "renderHeldItemTooltip",
        at = @At(value = "INVOKE", target = "net/minecraft/client/gui/hud/InGameHud.fill(IIIII)V")
    )
    private void fillBackgroundProxy(int x1, int y1, int x2, int y2, int color) {
        // On quitte si le fond est transparent
        if ((color & 0xff000000) == 0) return;

        // On récupère la largeur max si pas encore calculée
        if (maxWidth == Integer.MIN_VALUE) {
            for (InfoLine line : info) {
                if (line.width > maxWidth) maxWidth = line.width;
            }
        }

        // On fait le gros carré
        fill(
            (scaledWidth - maxWidth) / 2 - 2, y - 2,
            (scaledWidth + maxWidth) / 2 + 2, y + (int) (LINE_HEIGHT * info.size()) + 2,
            color
        );
    }



    /** Affiche le nom de l'item avec des infos en plus
     * @see InGameHud#renderHeldItemTooltip */
    @Redirect(
        method = "renderHeldItemTooltip",
        at = @At(value = "INVOKE", target = "net/minecraft/client/font/TextRenderer.drawWithShadow(Ljava/lang/String;FFI)I")
    )
    private int drawTextProxy(TextRenderer fontRenderer, String text, float _x, float _y, int color) {
        // Texte
        for (final InfoLine line : info) {
            final int x = (scaledWidth - line.width) / 2;
            fontRenderer.drawWithShadow(line.formatted, x, y, color);
            y += LINE_HEIGHT;
        }

        return 0;
    }



    /** Récupère le stack avant le tick pour gérer l'apparition du tooltip.
     * @see InGameHud#tick */
    @Inject(method = "tick", at = @At("HEAD"))
    public void onBeforeTick(CallbackInfo ci) {
        if (this.client.player != null) {
            stackBeforeTick = currentStack;
        }
    }

    /** Gère la création et l'apparition du tooltip.
     * @see InGameHud#tick */
    @Inject(method = "tick", at = @At("RETURN"))
    public void onAfterTick(CallbackInfo ci) {
        // Cas où y a rien à faire
        if (this.client.player == null
                || currentStack == stackBeforeTick
                || currentStack.isEmpty()) {
            return;
        }

        // Nouveau tooltip
        List<Text> newInfo = ItemInfo.buildInfo(currentStack);

        // On quitte si c'est pareil
        if (ItemInfo.areEqual(info, newInfo)) return;

        // Sinon on reset
        info = ItemInfo.toInfoLines(newInfo);
        maxWidth = Integer.MIN_VALUE;
        heldItemTooltipFade = 40 + 4 * (info.size() - 1);
    }

}
