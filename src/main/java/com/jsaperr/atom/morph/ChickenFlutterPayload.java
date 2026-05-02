package com.jsaperr.atom.morph;

import com.jsaperr.atom.Atom;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChickenFlutterPayload(boolean fluttering) implements CustomPacketPayload {

    public static final Type<ChickenFlutterPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Atom.MODID, "chicken_flutter"));

    public static final StreamCodec<FriendlyByteBuf, ChickenFlutterPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ChickenFlutterPayload::fluttering,
            ChickenFlutterPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
