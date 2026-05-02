package com.jsaperr.atom.morph;

import com.jsaperr.atom.Atom;
import com.jsaperr.atom.MorphCategories;
import com.jsaperr.atom.block.MorphRequestPayload;
import com.jsaperr.atom.morph.passive.MorphPassiveHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;
import java.util.Set;

@EventBusSubscriber(modid = Atom.MODID, bus = EventBusSubscriber.Bus.GAME)
public class MorphEventHandler {

    private static final ResourceLocation DEMORPH_SENTINEL =
            ResourceLocation.fromNamespaceAndPath("atom", "demorph");

    public static void handleMorphRequest(MorphRequestPayload payload, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        ResourceLocation mobId = payload.entityTypeId();

        if (mobId.equals(DEMORPH_SENTINEL)) {
            player.setData(MorphAttachments.PENDING_MORPH, Optional.of(DEMORPH_SENTINEL));
            player.displayClientMessage(Component.literal("Demorph queued - step onto the platform"), true);
            player.closeContainer();
            return;
        }

        Set<ResourceLocation> unlocked = player.getData(MorphUnlockAttachments.UNLOCKED_CATEGORIES);
        boolean allowed = MorphCategories.CATEGORIES.entrySet().stream()
                .anyMatch(e -> unlocked.contains(e.getKey()) && e.getValue().contains(mobId));
        if (!allowed) return;
        if (BuiltInRegistries.ENTITY_TYPE.get(mobId) == null) return;

        player.setData(MorphAttachments.PENDING_MORPH, Optional.of(mobId));
        player.displayClientMessage(Component.literal("Morph queued - step onto the platform"), true);
        player.closeContainer();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        player.getExistingData(MorphAttachments.ACTIVE_MORPH)
            .flatMap(opt -> opt)
            .ifPresent(type -> {
                player.refreshDimensions();
                MorphCommand.applyStepHeight(player, type);
                MorphPassiveHandler.applyPassives(player, type);
                ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                CompoundTag variantTag = MorphVariantHelper.randomVariantTag(type, player.serverLevel(), player.blockPosition());
                player.setData(MorphAttachments.ACTIVE_MORPH_VARIANT, variantTag);
                PacketDistributor.sendToPlayer(player,
                    new MorphSyncPayload(player.getUUID(), Optional.of(id), variantTag));
            });
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Optional<ResourceLocation> pending = player.getData(MorphAttachments.PENDING_MORPH);
        if (pending.isEmpty()) return;
        if (player.level().getBlockState(player.getOnPos()).getBlock() != Atom.MORPH_PLATFORM.get()) return;

        ResourceLocation mobId = pending.get();
        player.setData(MorphAttachments.PENDING_MORPH, Optional.empty());
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1.0f, 2.0f);

        if (mobId.equals(DEMORPH_SENTINEL)) {
            MorphPassiveHandler.stripPassives(player);
            player.setData(MorphAttachments.ACTIVE_MORPH, Optional.empty());
            player.setData(MorphAttachments.ACTIVE_MORPH_VARIANT, new CompoundTag());
            player.refreshDimensions();
            MorphCommand.resetStepHeight(player);
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                    new MorphSyncPayload(player.getUUID(), Optional.empty(), new CompoundTag()));
            return;
        }

        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(mobId);
        if (entityType == null) return;

        CompoundTag variantTag = MorphVariantHelper.randomVariantTag(entityType, player.serverLevel(), player.blockPosition());
        MorphPassiveHandler.stripPassives(player);
        player.setData(MorphAttachments.ACTIVE_MORPH, Optional.of(entityType));
        player.setData(MorphAttachments.ACTIVE_MORPH_VARIANT, variantTag);
        player.refreshDimensions();
        MorphCommand.applyStepHeight(player, entityType);
        MorphPassiveHandler.applyPassives(player, entityType);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new MorphSyncPayload(player.getUUID(), Optional.of(mobId), variantTag));
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer observer)) return;
        if (!(event.getTarget() instanceof Player tracked)) return;
        tracked.getExistingData(MorphAttachments.ACTIVE_MORPH)
            .flatMap(opt -> opt)
            .ifPresent(type -> {
                ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                CompoundTag variantTag = tracked.getData(MorphAttachments.ACTIVE_MORPH_VARIANT);
                PacketDistributor.sendToPlayer(observer,
                    new MorphSyncPayload(tracked.getUUID(), Optional.of(id), variantTag));
            });
    }
}
