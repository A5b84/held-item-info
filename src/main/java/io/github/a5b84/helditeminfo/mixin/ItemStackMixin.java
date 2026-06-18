package io.github.a5b84.helditeminfo.mixin;

import io.github.a5b84.helditeminfo.HeldItemInfo;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ItemStackMixin {

  @Inject(method = "addUnitComponentToTooltip", at = @At("HEAD"), cancellable = true)
  public void onAddUnitComponentToTooltip(
      DataComponentType<?> dataComponentType,
      Component component,
      TooltipDisplay display,
      Consumer<Component> builder,
      CallbackInfo ci) {
    if (dataComponentType == DataComponents.INTANGIBLE_PROJECTILE
        && HeldItemInfo.getIntangibleProjectileVisibility().isHidden()) {
      ci.cancel();
    }
  }
}
