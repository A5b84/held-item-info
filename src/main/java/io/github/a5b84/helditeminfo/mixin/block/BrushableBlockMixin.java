package io.github.a5b84.helditeminfo.mixin.block;

import io.github.a5b84.helditeminfo.Appenders;
import io.github.a5b84.helditeminfo.HeldItemInfo;
import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import io.github.a5b84.helditeminfo.config.HeldItemInfoConfig;
import io.github.a5b84.helditeminfo.mixin.BrushableBlockEntityAccessor;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.storage.TagValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BrushableBlock.class)
public abstract class BrushableBlockMixin implements TooltipAppender {

  @Override
  public boolean heldItemInfo_shouldAppendTooltip(HeldItemInfoConfig config) {
    return config.showContainerContent();
  }

  @Override
  public void heldItemInfo_appendTooltip(TooltipBuilder builder) {
    CompoundTag blockEntityData = builder.getBlockEntityData();

    if (blockEntityData != null) {
      readStack(builder, blockEntityData).ifPresent(stack -> Appenders.appendStack(builder, stack));
    }
  }

  @Unique
  private Optional<ItemStack> readStack(TooltipBuilder builder, CompoundTag data) {
    HolderLookup.Provider registries = builder.getTooltipContext().registries();

    if (registries == null) {
      return Optional.empty();
    }

    try (ProblemReporter.ScopedCollector reporter =
        new ProblemReporter.ScopedCollector(HeldItemInfo.LOGGER)) {
      return TagValueInput.create(reporter, registries, data)
          .read(BrushableBlockEntityAccessor.getItemTagKey(), ItemStack.CODEC);
    }
  }
}
