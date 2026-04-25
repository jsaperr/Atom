package com.jsaperr.atom;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

public class MorphAttachments {
    static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Atom.MODID);

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

    static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
