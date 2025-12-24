package io.github.a5b84.helditeminfo.mixin.block;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import io.github.a5b84.helditeminfo.Util;
import java.util.List;
import net.minecraft.block.CommandBlock;
import net.minecraft.text.MutableText;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandBlock.class)
public abstract class CommandBlockMixin implements TooltipAppender {

  @Override
  public boolean heldItemInfo_shouldAppendTooltip() {
    return config.showCommandBlockInfo();
  }

  @Override
  public void heldItemInfo_appendTooltip(TooltipBuilder builder) {
    builder
        .getBlockEntityData()
        .flatMap(blockEntityData -> blockEntityData.getString("Command"))
        .ifPresent(
            command -> {
              command = command.trim();
              if (!command.isEmpty()) {
                int maxLines = Math.min(config.maxCommandLines(), builder.getRemainingLines());
                List<MutableText> lines = Util.wrapLines(command, maxLines);

                for (MutableText text : lines) {
                  builder.append(() -> text.formatted(TooltipBuilder.DEFAULT_COLOR));
                }
              }
            });
  }
}
