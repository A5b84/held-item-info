package io.github.a5b84.helditeminfo.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemEnchantments.class)
public interface ItemEnchantmentsAccessor {
  @Invoker
  static <T> HolderSet<T> callGetTagOrEmpty(
      @Nullable HolderLookup.Provider registryLookup,
      ResourceKey<Registry<T>> registryRef,
      TagKey<T> tooltipOrderTag) {
    throw new AssertionError();
  }
}
