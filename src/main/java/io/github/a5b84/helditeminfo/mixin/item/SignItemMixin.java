package io.github.a5b84.helditeminfo.mixin.item;

import io.github.a5b84.helditeminfo.HeldItemInfo;
import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.SignItem;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SignItem.class)
public abstract class SignItemMixin implements TooltipAppender {

  @Override
  public boolean heldItemInfo_shouldAppendTooltip() {
    return HeldItemInfo.config.showSignText();
  }

  @Override
  public void heldItemInfo_appendTooltip(TooltipBuilder builder) {
    builder
        .getBlockEntityData()
        .ifPresent(
            blockEntityData -> {
              //noinspection DataFlowIssue
              RegistryOps<Tag> dynamicOps =
                  builder
                      .getTooltipContext()
                      .registries()
                      .createSerializationContext(NbtOps.INSTANCE);
              List<MutableComponent> frontLines =
                  buildSide(blockEntityData, "front_text", dynamicOps);
              List<MutableComponent> backLines =
                  buildSide(blockEntityData, "back_text", dynamicOps);

              if (!frontLines.isEmpty()) {
                builder.appendAll(frontLines);
              }

              if (!backLines.isEmpty()) {
                if (!frontLines.isEmpty()) {
                  builder.append(
                      Component.translatable("held_item_info.tooltip.sign_side_separator")
                          .withStyle(TooltipBuilder.DEFAULT_COLOR));
                }

                builder.appendAll(backLines);
              }
            });
  }

  @Unique
  private List<MutableComponent> buildSide(
      CompoundTag blockEntityNbt, String sideKey, RegistryOps<Tag> dynamicOps) {
    return blockEntityNbt
        .getCompound(sideKey)
        .flatMap(
            sideData ->
                SignText.DIRECT_CODEC
                    .parse(dynamicOps, sideData)
                    .resultOrPartial(HeldItemInfo.LOGGER::error))
        .map(
            signText -> {
              Component[] messages =
                  signText.getMessages(Minecraft.getInstance().isTextFilteringEnabled());
              List<MutableComponent> lines = new ArrayList<>(messages.length);

              for (Component message : messages) {
                if (message != null) {
                  String messageStr = message.getString();
                  if (!messageStr.isBlank()) {
                    lines.add(
                        Component.literal(messageStr).withStyle(TooltipBuilder.DEFAULT_COLOR));
                  }
                }
              }

              return lines;
            })
        .orElse(Collections.emptyList());
  }
}
