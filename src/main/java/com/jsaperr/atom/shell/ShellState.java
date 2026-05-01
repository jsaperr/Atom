package com.jsaperr.atom.shell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.Optional;
import java.util.UUID;

public record ShellState(
        UUID uuid,
        ShellPosition position,
        float health,
        int foodLevel,
        float saturation,
        float exhaustion,
        int xpLevel,
        float xpProgress,
        CompoundTag inventoryNbt,
        Optional<ResourceLocation> morph
) {
    public static final Codec<ShellState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(ShellState::uuid),
            ShellPosition.CODEC.fieldOf("position").forGetter(ShellState::position),
            Codec.FLOAT.fieldOf("health").forGetter(ShellState::health),
            Codec.INT.fieldOf("foodLevel").forGetter(ShellState::foodLevel),
            Codec.FLOAT.fieldOf("saturation").forGetter(ShellState::saturation),
            Codec.FLOAT.fieldOf("exhaustion").forGetter(ShellState::exhaustion),
            Codec.INT.fieldOf("xpLevel").forGetter(ShellState::xpLevel),
            Codec.FLOAT.fieldOf("xpProgress").forGetter(ShellState::xpProgress),
            CompoundTag.CODEC.fieldOf("inventory").forGetter(ShellState::inventoryNbt),
            ResourceLocation.CODEC.optionalFieldOf("morph").forGetter(ShellState::morph)
    ).apply(instance, ShellState::new));

    public static CompoundTag snapshotInventory(Inventory inventory) {
        CompoundTag tag = new CompoundTag();
        tag.put("Items", inventory.save(new ListTag()));
        return tag;
    }
}
