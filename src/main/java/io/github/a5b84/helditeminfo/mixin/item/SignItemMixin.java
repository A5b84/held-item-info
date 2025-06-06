package io.github.a5b84.helditeminfo.mixin.item;

import io.github.a5b84.helditeminfo.HeldItemInfo;
import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.SignItem;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

@Mixin(SignItem.class)
public abstract class SignItemMixin implements TooltipAppender {

    @Override
    public boolean heldItemInfo_shouldAppendTooltip() {
        return config.showSignText();
    }

    @Override
    public void heldItemInfo_appendTooltip(TooltipBuilder builder) {
        builder.getComponentForDisplay(DataComponentTypes.BLOCK_ENTITY_DATA)
                .ifPresent(blockEntityData -> {
                    //noinspection DataFlowIssue ("Method invocation 'getOps' may produce 'NullPointerException'")
                    RegistryOps<NbtElement> dynamicOps = builder.getTooltipContext().getRegistryLookup().getOps(NbtOps.INSTANCE);
                    List<MutableText> frontLines = buildSide(blockEntityData, "front_text", dynamicOps);
                    List<MutableText> backLines = buildSide(blockEntityData, "back_text", dynamicOps);

                    if (!frontLines.isEmpty()) {
                        builder.appendAll(frontLines);
                        if (!backLines.isEmpty()) {
                            builder.append(Text.literal("-------").formatted(TooltipBuilder.DEFAULT_COLOR));
                        }
                    }

                    if (!backLines.isEmpty()) {
                        builder.appendAll(backLines);
                    }
                });
    }

    @Unique
    private List<MutableText> buildSide(NbtComponent blockEntityData, String sideKey, RegistryOps<NbtElement> dynamicOps) {
        //noinspection deprecation (getNbt)
        return blockEntityData.getNbt()
                .getCompound(sideKey)
                .flatMap(sideData -> SignText.CODEC.parse(dynamicOps, sideData).resultOrPartial(HeldItemInfo.LOGGER::error))
                .map(signText -> {
                    Text[] messages = signText.getMessages(MinecraftClient.getInstance().shouldFilterText());
                    List<MutableText> lines = new ArrayList<>(messages.length);

                    for (Text message : messages) {
                        if (message != null) {
                            String messageStr = message.getString();
                            if (!messageStr.isBlank()) {
                                lines.add(Text.literal(messageStr).formatted(TooltipBuilder.DEFAULT_COLOR));
                            }
                        }
                    }

                    return lines;
                })
                 .orElse(Collections.emptyList());
    }
}
