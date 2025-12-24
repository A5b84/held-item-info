package io.github.a5b84.helditeminfo.mixin;

import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrialSpawnerStateData.class)
public interface TrialSpawnerStateDataAccessor {
  @Accessor("TAG_SPAWN_DATA")
  static String getSpawnDataKey() {
    throw new AssertionError();
  }
}
