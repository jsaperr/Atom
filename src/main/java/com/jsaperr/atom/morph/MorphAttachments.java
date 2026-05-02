package com.jsaperr.atom.morph;

import com.jsaperr.atom.Atom;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class MorphAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Atom.MODID);

    public static final Supplier<AttachmentType<Set<ResourceLocation>>> MORPH_ACTIVE_EFFECTS =
        ATTACHMENT_TYPES.register("morph_active_effects", () ->
            AttachmentType.<Set<ResourceLocation>>builder(() -> new HashSet<>())
                .build()
        );

    public static final Supplier<AttachmentType<CompoundTag>> ACTIVE_MORPH_VARIANT =
        ATTACHMENT_TYPES.register("active_morph_variant", () ->
            AttachmentType.<CompoundTag>builder(() -> new CompoundTag())
                .build()
        );

    public static final Supplier<AttachmentType<Optional<EntityType<?>>>> ACTIVE_MORPH =
        ATTACHMENT_TYPES.register("active_morph", () ->
            AttachmentType.<Optional<EntityType<?>>>builder(() -> Optional.empty())
                .serialize(
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec()
                        .optionalFieldOf("entity_type")
                        .codec()
                )
                .build()
        );

    public static final Supplier<AttachmentType<Optional<ResourceLocation>>> PENDING_MORPH =
        ATTACHMENT_TYPES.register("pending_morph", () ->
            AttachmentType.<Optional<ResourceLocation>>builder(() -> Optional.empty())
                .build()
        );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
