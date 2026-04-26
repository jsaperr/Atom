package com.jsaperr.atom.client;

import com.jsaperr.atom.MorphAttachments;
import com.jsaperr.atom.MorphSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Optional;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientPayloadHandler {
    public static void register(PayloadRegistrar registrar) {
        registrar.playToClient(MorphSyncPayload.TYPE, MorphSyncPayload.STREAM_CODEC,
            ClientPayloadHandler::handleMorphSync);
    }

    public static void handleMorphSync(MorphSyncPayload payload, IPayloadContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        UUID targetUuid = payload.playerUuid();
        Optional<EntityType<?>> resolved = payload.entityTypeId()
            .flatMap(id -> BuiltInRegistries.ENTITY_TYPE.getOptional(id));

        MorphPuppetManager.setMorph(targetUuid, resolved);

        if (mc.player != null && mc.player.getUUID().equals(targetUuid)) {
            mc.player.setData(MorphAttachments.ACTIVE_MORPH, resolved);
            mc.player.refreshDimensions();
        }
    }
}
