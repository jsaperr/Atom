package com.jsaperr.atom;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ShellAttachments {
    private static final DeferredRegister<AttachmentType<?>> REGISTER =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Atom.MODID);

    public static final Supplier<AttachmentType<List<ShellState>>> SHELLS =
            REGISTER.register("shells", () -> AttachmentType
                    .<List<ShellState>>builder((Supplier<List<ShellState>>) ArrayList::new)
                    .serialize(ShellState.CODEC.listOf())
                    .build());

    public static void register(IEventBus modBus) {
        REGISTER.register(modBus);
    }
}
