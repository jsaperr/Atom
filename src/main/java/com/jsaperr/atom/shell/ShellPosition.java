package com.jsaperr.atom.shell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record ShellPosition(
        ResourceLocation dimension,
        double x,
        double y,
        double z,
        float yRot,
        float xRot
) {
    public static final Codec<ShellPosition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("dimension").forGetter(ShellPosition::dimension),
            Codec.DOUBLE.fieldOf("x").forGetter(ShellPosition::x),
            Codec.DOUBLE.fieldOf("y").forGetter(ShellPosition::y),
            Codec.DOUBLE.fieldOf("z").forGetter(ShellPosition::z),
            Codec.FLOAT.fieldOf("yRot").forGetter(ShellPosition::yRot),
            Codec.FLOAT.fieldOf("xRot").forGetter(ShellPosition::xRot)
    ).apply(instance, ShellPosition::new));
}
