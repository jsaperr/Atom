package com.jsaperr.atom.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public record ResearchSyncPayload(BlockPos pos, Set<ResourceLocation> researched) implements CustomPacketPayload {

    public static final Type<ResearchSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atom", "research_sync"));

    public static final StreamCodec<FriendlyByteBuf, ResearchSyncPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ResearchSyncPayload::pos,
            ByteBufCodecs.collection(HashSet::new, ResourceLocation.STREAM_CODEC), ResearchSyncPayload::researched,
            ResearchSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
