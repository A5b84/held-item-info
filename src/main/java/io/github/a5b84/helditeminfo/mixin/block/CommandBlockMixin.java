package io.github.a5b84.helditeminfo.mixin.block;

import io.github.a5b84.helditeminfo.HeldItemInfo;
import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import io.github.a5b84.helditeminfo.Util;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.CommandBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandBlock.class)
public abstract class CommandBlockMixin implements TooltipAppender {

  @Override
  public boolean heldItemInfo_shouldAppendTooltip() {
    return HeldItemInfo.getConfig().showCommandBlockInfo();
  }

  @Override
  public void heldItemInfo_appendTooltip(TooltipBuilder builder) {
    CompoundTag blockEntityData = builder.getBlockEntityData();

    if (blockEntityData != null) {
      blockEntityData
          .getString("Command")
          .ifPresent(
              command -> {
                command = command.trim();
                if (!command.isEmpty()) {
                  int maxLines =
                      Math.min(
                          HeldItemInfo.getConfig().maxCommandLines(), builder.getRemainingLines());
                  List<MutableComponent> lines = Util.wrapLines(command, maxLines);

                  for (MutableComponent text : lines) {
                    builder.append(() -> text.withStyle(TooltipBuilder.DEFAULT_COLOR));
                  }
                }
              });
    }
  }
}
