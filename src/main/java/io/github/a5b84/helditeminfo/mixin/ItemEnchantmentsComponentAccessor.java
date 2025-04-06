package io.github.a5b84.helditeminfo.mixin;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemEnchantmentsComponent.class)
public interface ItemEnchantmentsComponentAccessor {
    @Invoker
    static <T> RegistryEntryList<T> callGetTooltipOrderList(@Nullable RegistryWrapper.WrapperLookup registryLookup, RegistryKey<Registry<T>> registryRef, TagKey<T> tooltipOrderTag) {
        throw new AssertionError();
    }
}
