package io.github.a5b84.helditeminfo.mixin.block;

import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.TrialSpawnerBlock;
import net.minecraft.block.entity.Spawner;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.block.spawner.TrialSpawnerData;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

@Mixin({SpawnerBlock.class, TrialSpawnerBlock.class})
public class SpawnerBlocksMixin implements TooltipAppender {

    @Override
    public boolean heldItemInfo_shouldAppendTooltip() {
        return config.showSpawnerEntity();
    }

    @Override
    public void heldItemInfo_appendTooltip(TooltipBuilder builder) {
        String spawnDataKey = ((Object) this instanceof TrialSpawnerBlock)
                ? TrialSpawnerData.SPAWN_DATA_KEY
                : MobSpawnerLogic.SPAWN_DATA_KEY;
        builder.getComponentForDisplay(DataComponentTypes.BLOCK_ENTITY_DATA)
                .ifPresent(blockEntityData -> {
                    Text text = Spawner.getSpawnedEntityText(blockEntityData, spawnDataKey);
                    if (text != null) {
                        builder.append(text);
                    }
                });
    }
}
