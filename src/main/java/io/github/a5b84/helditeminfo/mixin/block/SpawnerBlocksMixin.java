package io.github.a5b84.helditeminfo.mixin.block;

import io.github.a5b84.helditeminfo.HeldItemInfo;
import io.github.a5b84.helditeminfo.TooltipAppender;
import io.github.a5b84.helditeminfo.TooltipBuilder;
import io.github.a5b84.helditeminfo.mixin.TrialSpawnerStateDataAccessor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({SpawnerBlock.class, TrialSpawnerBlock.class})
public class SpawnerBlocksMixin implements TooltipAppender {

  @Override
  public boolean heldItemInfo_shouldAppendTooltip() {
    return HeldItemInfo.config.showSpawnerEntity();
  }

  @Override
  public void heldItemInfo_appendTooltip(TooltipBuilder builder) {
    String spawnDataKey =
        ((Object) this instanceof TrialSpawnerBlock)
            ? TrialSpawnerStateDataAccessor.getSpawnDataKey()
            : BaseSpawner.SPAWN_DATA_TAG;
    builder
        .getComponentForDisplay(DataComponents.BLOCK_ENTITY_DATA)
        .ifPresent(
            blockEntityData -> {
              Component text = Spawner.getSpawnEntityDisplayName(blockEntityData, spawnDataKey);
              if (text != null) {
                builder.append(text);
              }
            });
  }
}
