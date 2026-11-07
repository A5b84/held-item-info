package io.github.a5b84.helditeminfo.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.a5b84.helditeminfo.HeldItemTooltipContext;
import java.awt.*;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEnchantments.class)
public abstract class ItemEnchantmentsMixin {

  @WrapWithCondition(
      method = "addToTooltip",
      at =
          @At(
              value = "INVOKE",
              target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
  private boolean shouldAddToTooltip(
      Consumer<Component> instance,
      Object component,
      @Local(name = "enchantment") Holder<Enchantment> enchantment) {
    HeldItemTooltipContext context = HeldItemTooltipContext.instance;

    if (context == null || !context.filterEnchantments) {
      return true;
    }

    Set<Identifier> filteredEnchantments = context.config.filteredEnchantments();

    if (filteredEnchantments.isEmpty()) {
      return true;
    }

    Identifier id = enchantment.unwrapKey().map(ResourceKey::identifier).orElse(null);
    return filteredEnchantments.contains(id) == context.config.showOnlyFilteredEnchantments();
  }
}
