package com.jsaperr.atom.block;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MorphRequestPayload(ResourceLocation entityTypeId) implements CustomPacketPayload {

    public static final Type<MorphRequestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atom", "morph_request"));

    public static final StreamCodec<FriendlyByteBuf, MorphRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            MorphRequestPayload::entityTypeId,
            MorphRequestPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
