package com.jsaperr.atom;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.UUID;

public record MorphSyncPayload(UUID playerUuid, Optional<ResourceLocation> entityTypeId) implements CustomPacketPayload {
    public static final Type<MorphSyncPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(Atom.MODID, "morph_sync"));

    public static final StreamCodec<ByteBuf, MorphSyncPayload> STREAM_CODEC = StreamCodec.composite(
        UUIDUtil.STREAM_CODEC,
        MorphSyncPayload::playerUuid,
        ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC),
        MorphSyncPayload::entityTypeId,
        MorphSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
