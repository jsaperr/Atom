package com.jsaperr.atom.client;

import com.jsaperr.atom.block.MorphRequestPayload;
import com.jsaperr.atom.block.MorphSelectPayload;
import com.jsaperr.atom.block.ResearchSyncPayload;
import com.jsaperr.atom.block.gui.MorphPlatformScreen;
import com.jsaperr.atom.block.gui.ResearchStationScreen;
import com.jsaperr.atom.morph.MorphAttachments;
import com.jsaperr.atom.morph.MorphSyncPayload;
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
        registrar.playToClient(ResearchSyncPayload.TYPE, ResearchSyncPayload.STREAM_CODEC,
            ClientPayloadHandler::handleResearchSync);
        registrar.playToClient(MorphSelectPayload.TYPE, MorphSelectPayload.STREAM_CODEC,
            ClientPayloadHandler::handleMorphSelect);
    }

    public static void handleMorphSync(MorphSyncPayload payload, IPayloadContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        UUID targetUuid = payload.playerUuid();
        Optional<EntityType<?>> resolved = payload.entityTypeId()
            .flatMap(id -> BuiltInRegistries.ENTITY_TYPE.getOptional(id));

        MorphPuppetManager.setMorph(targetUuid, resolved, payload.variantTag());

        if (mc.player != null && mc.player.getUUID().equals(targetUuid)) {
            mc.player.setData(MorphAttachments.ACTIVE_MORPH, resolved);
            mc.player.setData(MorphAttachments.ACTIVE_MORPH_VARIANT, payload.variantTag());
            mc.player.refreshDimensions();
        }
    }

    public static void handleResearchSync(ResearchSyncPayload payload, IPayloadContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof ResearchStationScreen screen) {
            screen.setResearched(payload.researched());
        }
    }

    public static void handleMorphSelect(MorphSelectPayload payload, IPayloadContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof MorphPlatformScreen screen) {
            screen.setAvailableMobs(payload.availableMobs());
        }
    }
}
