package com.jsaperr.atom.client;

import com.jsaperr.atom.MorphAttachments;
import com.jsaperr.atom.MorphSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class ClientPayloadHandler {
    public static void handleMorphSync(MorphSyncPayload payload, IPayloadContext ctx) {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        Optional<EntityType<?>> resolved = payload.entityTypeId()
            .flatMap(id -> BuiltInRegistries.ENTITY_TYPE.getOptional(id));
        player.setData(MorphAttachments.ACTIVE_MORPH, resolved);
        player.refreshDimensions();
        MorphPuppetManager.setMorph(player.getUUID(), resolved);
    }
}
