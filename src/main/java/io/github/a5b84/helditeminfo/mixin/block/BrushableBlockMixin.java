package io.github.a5b84.helditeminfo.mixin.block;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

import io.github.a5b84.helditeminfo.Appenders;
import io.github.a5b84.helditeminfo.HeldItemInfo;
import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import io.github.a5b84.helditeminfo.mixin.BrushableBlockEntityAccessor;
import java.util.Optional;
import net.minecraft.block.BrushableBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.NbtReadView;
import net.minecraft.util.ErrorReporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BrushableBlock.class)
public abstract class BrushableBlockMixin implements TooltipAppender {

  @Override
  public boolean heldItemInfo_shouldAppendTooltip() {
    return config.showContainerContent();
  }

  @Override
  public void heldItemInfo_appendTooltip(TooltipBuilder builder) {
    builder
        .getBlockEntityData()
        .flatMap(blockEntityData -> readStack(builder, blockEntityData))
        .ifPresent(stack -> Appenders.appendStack(builder, stack));
  }

  @Unique
  private Optional<ItemStack> readStack(TooltipBuilder builder, NbtCompound data) {
    try (ErrorReporter.Logging reporter = new ErrorReporter.Logging(HeldItemInfo.LOGGER)) {
      //noinspection DataFlowIssue (Argument 'builder.tooltipContext.getRegistryLookup()' might be
      // null)
      return NbtReadView.create(reporter, builder.getTooltipContext().getRegistryLookup(), data)
          .read(BrushableBlockEntityAccessor.getItemNbtKey(), ItemStack.CODEC);
    }
  }
}
