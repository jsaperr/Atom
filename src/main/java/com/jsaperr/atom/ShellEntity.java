package com.jsaperr.atom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class ShellEntity extends Entity {
    private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID =
            SynchedEntityData.defineId(ShellEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> OWNER_NAME =
            SynchedEntityData.defineId(ShellEntity.class, EntityDataSerializers.STRING);

    @Nullable private ShellState state;

    public ShellEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public ShellEntity(EntityType<?> type, Level level, ShellState state, UUID ownerUuid, String ownerName) {
        super(type, level);
        this.state = state;
        this.entityData.set(OWNER_UUID, Optional.of(ownerUuid));
        this.entityData.set(OWNER_NAME, ownerName);
        this.setUUID(state.uuid());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER_UUID, Optional.empty());
        builder.define(OWNER_NAME, "");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("state")) {
            state = ShellState.CODEC.parse(NbtOps.INSTANCE, tag.get("state"))
                    .resultOrPartial(e -> {}).orElse(null);
        }
        if (tag.hasUUID("ownerUuid")) entityData.set(OWNER_UUID, Optional.of(tag.getUUID("ownerUuid")));
        entityData.set(OWNER_NAME, tag.getString("ownerName"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (state != null) {
            ShellState.CODEC.encodeStart(NbtOps.INSTANCE, state)
                    .resultOrPartial(e -> {})
                    .ifPresent(nbt -> tag.put("state", nbt));
        }
        getOwnerUuid().ifPresent(uuid -> tag.putUUID("ownerUuid", uuid));
        tag.putString("ownerName", entityData.get(OWNER_NAME));
    }

    @Nullable
    public ShellState getState() { return state; }
    public Optional<UUID> getOwnerUuid() { return entityData.get(OWNER_UUID); }
    public String getOwnerName() { return entityData.get(OWNER_NAME); }

    public Optional<ResourceLocation> getMorph() {
        return state != null ? state.morph() : Optional.empty();
    }

    // TODO: interact() — wire up to shell pod block when interaction mechanic is decided
}
