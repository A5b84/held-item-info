package io.github.a5b84.helditeminfo.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class ItemEnchantmentsBuilder {

  private final HolderLookup.RegistryLookup<Enchantment> lookup;
  private final ItemEnchantments.Mutable enchantments =
      new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

  public ItemEnchantmentsBuilder(HolderLookup.Provider lookupProvider) {
    this.lookup = lookupProvider.lookupOrThrow(Registries.ENCHANTMENT);
  }

  public ItemEnchantmentsBuilder set(ResourceKey<Enchantment> enchantment, int level) {
    enchantments.set(lookup.getOrThrow(enchantment), level);
    return this;
  }

  public ItemEnchantments build() {
    return enchantments.toImmutable();
  }
}
