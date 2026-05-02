package com.jsaperr.atom.block;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record MorphSelectPayload(List<ResourceLocation> availableMobs) implements CustomPacketPayload {

    public static final Type<MorphSelectPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("atom", "morph_select"));

    public static final StreamCodec<FriendlyByteBuf, MorphSelectPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ResourceLocation.STREAM_CODEC),
            MorphSelectPayload::availableMobs,
            MorphSelectPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
