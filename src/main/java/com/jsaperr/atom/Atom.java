package com.jsaperr.atom;

import com.jsaperr.atom.item.ExtractorItem;
import com.jsaperr.atom.morph.MorphAttachments;
import com.jsaperr.atom.morph.MorphCommand;
import com.jsaperr.atom.morph.MorphSyncPayload;
import com.jsaperr.atom.shell.ShellAttachments;
import com.jsaperr.atom.shell.ShellEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

@Mod(Atom.MODID)
public class Atom {
    public static final String MODID = "atom";

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, MODID);

    public static final Supplier<ExtractorItem> EXTRACTOR =
            ITEMS.register("extractor", () -> new ExtractorItem(new Item.Properties().stacksTo(1)));

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);

    public static final Supplier<EntityType<ShellEntity>> SHELL_ENTITY =
            ENTITY_TYPES.register("shell", () ->
                    EntityType.Builder.<ShellEntity>of(ShellEntity::new, MobCategory.MISC)
                            .sized(0.6f, 1.8f)
                            .build("atom:shell"));

    public Atom(IEventBus modEventBus, ModContainer modContainer) {
        MorphAttachments.register(modEventBus);
        ShellAttachments.register(modEventBus);
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
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
                    MorphCommand.applyStepHeight(player, type);
                    ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                    PacketDistributor.sendToPlayer(player,
                        new MorphSyncPayload(player.getUUID(), Optional.of(id)));
                });
        }

        @SubscribeEvent
        public static void onStartTracking(PlayerEvent.StartTracking event) {
            if (!(event.getEntity() instanceof ServerPlayer observer)) return;
            if (!(event.getTarget() instanceof Player tracked)) return;
            tracked.getExistingData(MorphAttachments.ACTIVE_MORPH)
                .flatMap(opt -> opt)
                .ifPresent(type -> {
                    ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                    PacketDistributor.sendToPlayer(observer,
                        new MorphSyncPayload(tracked.getUUID(), Optional.of(id)));
                });
        }
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class NetworkEvents {
        @SubscribeEvent
        public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
            var registrar = event.registrar("1");
            if (FMLEnvironment.dist == Dist.CLIENT) {
                com.jsaperr.atom.client.ClientPayloadHandler.register(registrar);
            } else {
                registrar.playToClient(MorphSyncPayload.TYPE, MorphSyncPayload.STREAM_CODEC, (p, c) -> {});
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(Atom.SHELL_ENTITY.get(),
                    com.jsaperr.atom.client.ShellEntityRenderer::new);
        }
    }
}
