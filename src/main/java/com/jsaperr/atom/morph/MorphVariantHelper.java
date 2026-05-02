package com.jsaperr.atom.morph;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

public class MorphVariantHelper {
    public static CompoundTag randomVariantTag(EntityType<?> type, ServerLevel level, BlockPos pos) {
        var entity = type.create(level);
        if (!(entity instanceof Mob mob)) return new CompoundTag();
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.COMMAND, null);
        // finalizeSpawn for Frog and Wolf picks from the biome at (0,0,0) — always deterministic.
        // Override with a true random pick from the full variant registry.
        if (mob instanceof Frog frog) {
            var variants = level.registryAccess().registryOrThrow(Registries.FROG_VARIANT).holders().toList();
            if (!variants.isEmpty()) frog.setVariant(variants.get(level.getRandom().nextInt(variants.size())));
        } else if (mob instanceof Wolf wolf) {
            var variants = level.registryAccess().registryOrThrow(Registries.WOLF_VARIANT).holders().toList();
            if (!variants.isEmpty()) wolf.setVariant(variants.get(level.getRandom().nextInt(variants.size())));
        }
        return mob.saveWithoutId(new CompoundTag());
    }
}
