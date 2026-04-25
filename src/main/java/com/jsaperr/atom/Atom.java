package com.jsaperr.atom;

import com.jsaperr.atom.client.ClientPayloadHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.Optional;

@Mod(Atom.MODID)
public class Atom {
    public static final String MODID = "atom";

    public Atom(IEventBus modEventBus, ModContainer modContainer) {
        MorphAttachments.register(modEventBus);
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {
        @SubscribeEvent
        public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;
            player.getExistingData(MorphAttachments.ACTIVE_MORPH)
                .flatMap(opt -> opt)
                .ifPresent(type -> {
                    player.refreshDimensions();
                    ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                    PacketDistributor.sendToPlayer(player,
                        new MorphSyncPayload(Optional.of(id)));
                });
        }
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class NetworkEvents {
        @SubscribeEvent
        public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
            event.registrar("1").playToClient(
                MorphSyncPayload.TYPE,
                MorphSyncPayload.STREAM_CODEC,
                ClientPayloadHandler::handleMorphSync
            );
        }
    }
}
