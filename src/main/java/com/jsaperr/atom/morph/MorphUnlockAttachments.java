package com.jsaperr.atom.morph;

import com.jsaperr.atom.Atom;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class MorphUnlockAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Atom.MODID);

    public static final Supplier<AttachmentType<Set<ResourceLocation>>> UNLOCKED_CATEGORIES =
            ATTACHMENT_TYPES.register("unlocked_categories", () ->
                    AttachmentType.<Set<ResourceLocation>>builder((Supplier<Set<ResourceLocation>>) HashSet::new)
                            .serialize(ResourceLocation.CODEC.listOf().xmap(HashSet::new, l -> l.stream().toList()))
                            .build()
            );

    public static void register(IEventBus modBus) {
        ATTACHMENT_TYPES.register(modBus);
    }
}
