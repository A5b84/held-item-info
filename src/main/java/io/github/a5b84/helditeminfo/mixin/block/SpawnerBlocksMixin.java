package io.github.a5b84.helditeminfo.mixin.block;

import static io.github.a5b84.helditeminfo.HeldItemInfo.config;

import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import io.github.a5b84.helditeminfo.mixin.TrialSpawnerDataAccessor;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.TrialSpawnerBlock;
import net.minecraft.block.entity.Spawner;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({SpawnerBlock.class, TrialSpawnerBlock.class})
public class SpawnerBlocksMixin implements TooltipAppender {

  @Override
  public boolean heldItemInfo_shouldAppendTooltip() {
    return config.showSpawnerEntity();
  }

  @Override
  public void heldItemInfo_appendTooltip(TooltipBuilder builder) {
    String spawnDataKey =
        ((Object) this instanceof TrialSpawnerBlock)
            ? TrialSpawnerDataAccessor.getSpawnDataKey()
            : MobSpawnerLogic.SPAWN_DATA_KEY;
    builder
        .getComponentForDisplay(DataComponentTypes.BLOCK_ENTITY_DATA)
        .ifPresent(
            blockEntityData -> {
              Text text = Spawner.getSpawnedEntityText(blockEntityData, spawnDataKey);
              if (text != null) {
                builder.append(text);
              }
            });
  }
}
