package io.github.a5b84.helditeminfo.mixin;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.a5b84.helditeminfo.ItemInfo;
import io.github.a5b84.helditeminfo.ItemInfo.InfoLine;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.ItemStack;

/**
 * Mixin qui affiche le widget du mod après les autres overlays.
 */
@Mixin(InGameHud.class)
public abstract class HeldItemTooltipMixin extends DrawableHelper {

    // Trucs de InGameHud
    @Shadow private @Final MinecraftClient client;
    @Shadow private int heldItemTooltipFade;
    @Shadow private ItemStack currentStack;
    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;

    @Shadow
    public TextRenderer getFontRenderer() { return null; }



    protected List<InfoLine> info = null;

    protected ItemStack stackBeforeTick;



    /**
     * Affiche le tooltip de l'item avec des informations en plus
     * (ou laisse la méthode de base faire son truc si y a rien à ajouter).
     * @see InGameHud#renderHeldItemTooltip
     * @see ItemInfo#buildInfo
     */
    @Inject(method = "renderHeldItemTooltip", at = @At("HEAD"), cancellable = true)
    public void onRenderHeldItemTooltip(CallbackInfo ci) {
        client.getProfiler().push("selectedItemName");

        // if () {} -> if (!) return;
        if (heldItemTooltipFade <= 0 || currentStack.isEmpty()) return;

        // Texte du tooltip modifié dans #onAfterTick

        // On laisse le jeu vanilla faire si on a rien à ajouter
        if (info == null) return;
        ci.cancel(); // Sinon on va le faire nous même

        // Ajouté pour simplifier
        final TextRenderer fontRenderer = getFontRenderer();

        // Valeurs communes (déplacées de plus loin)
        int alpha = (int) (heldItemTooltipFade * 25.6f);
        if (alpha <= 0) return;
        if (alpha > 255) alpha = 255;

        final int color = 0xffffff + (alpha << 24);
        int y = scaledHeight - 50 - (int) ((fontRenderer.fontHeight - 2.5) * info.size());
        //  (fontHeight - .) pour pas que ça prenne trop de place
        if (!this.client.interactionManager.hasStatusBars()) y += 14;

        // C'est parti
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Arrière-plan
        final int bgColor = client.options.getTextBackgroundColor(0);

        if ((bgColor & 0xff000000) != 0) {
            // On récupèrel la largeur max
            int maxWidth = info.get(0).width;
            for (int i = 1; i < info.size(); i++) {
                if (info.get(i).width > maxWidth) maxWidth = info.get(i).width;
            }
            
            // On fait un gros carré
            fill(
                (scaledWidth - maxWidth) / 2 - 2, y - 2,
                (scaledWidth + maxWidth) / 2 + 2, y + (fontRenderer.fontHeight - 1) * info.size() + 2,
                bgColor
            );
        }

        // Texte
        for (final InfoLine line : info) {
            final int x = (scaledWidth - line.width) / 2;
            fontRenderer.drawWithShadow(line.formatted, x, y, color);
            y += fontRenderer.fontHeight - 1;
        }

        // C'est fini
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
        client.getProfiler().pop();
    }

    /**
     * Récupère le stack avant le tick pour gérer l'apparition du tooltip.
     * @see InGameHud#tick
     */
    @Inject(method = "tick", at = @At("HEAD"))
    public void onBeforeTick(CallbackInfo ci) {
        if (this.client.player != null) {
            stackBeforeTick = currentStack;
        }
    }

    /**
     * Gère la création et l'apparition du tooltip.
     * @see InGameHud#tick
     */
    @Inject(method = "tick", at = @At("RETURN"))
    public void onAfterTick(CallbackInfo ci) {
        // Cas où y a rienn à faire
        if (this.client.player == null
                || currentStack == stackBeforeTick
                || currentStack.isEmpty()) {
            return;
        }

        // Nouveau tooltip
        List<InfoLine> newInfo = ItemInfo.buildInfo(currentStack);
        if (newInfo.size() <= 1) newInfo = null; // null si y a que le nom

        // On laisse le jeu gérer si on a rien à faire
        if (info == null && newInfo == null) return;

        // Le stack a changé + on avait un tooltip et on en a encore un
        //  => le tooltip a (sûrement) changé
        //  => on reset le timer
        info = newInfo;
        heldItemTooltipFade = 40 + 4 * (info == null ? 0 : info.size() - 1);
    }

}
