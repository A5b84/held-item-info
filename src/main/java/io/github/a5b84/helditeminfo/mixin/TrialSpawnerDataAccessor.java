package io.github.a5b84.helditeminfo.mixin;

import net.minecraft.block.spawner.TrialSpawnerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrialSpawnerData.class)
public interface TrialSpawnerDataAccessor {
  @Accessor("SPAWN_DATA_KEY")
  static String getSpawnDataKey() {
    throw new AssertionError();
  }
}
