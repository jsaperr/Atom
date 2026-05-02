package com.jsaperr.atom.morph.passive;

import com.jsaperr.atom.Atom;
import com.jsaperr.atom.morph.ChickenFlutterPayload;
import com.jsaperr.atom.morph.MorphAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Holder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = Atom.MODID, bus = EventBusSubscriber.Bus.GAME)
public class MorphPassiveHandler {

    private static final ResourceLocation MORPH_ARMOR_ID = ResourceLocation.fromNamespaceAndPath(Atom.MODID, "morph_armor");
    private static final ResourceLocation MORPH_JUMP_ID  = ResourceLocation.fromNamespaceAndPath(Atom.MODID, "morph_jump");
    private static final ResourceLocation MORPH_SWIM_ID  = ResourceLocation.fromNamespaceAndPath(Atom.MODID, "morph_swim");

    private static final List<Holder<MobEffect>> MANAGED_EFFECTS = List.of(
            MobEffects.NIGHT_VISION,
            MobEffects.FIRE_RESISTANCE,
            MobEffects.SLOW_FALLING
    );

    public static void applyPassives(ServerPlayer player, EntityType<?> type) {
        if (type == EntityType.FOX) {
            addEffect(player, MobEffects.NIGHT_VISION);
        } else if (type == EntityType.RABBIT) {
            addModifier(player, Attributes.JUMP_STRENGTH, MORPH_JUMP_ID, 0.3);
        } else if (type == EntityType.TURTLE) {
            addModifier(player, Attributes.ARMOR, MORPH_ARMOR_ID, 2.0);
            addModifier(player, Attributes.WATER_MOVEMENT_EFFICIENCY, MORPH_SWIM_ID, 0.8);
        } else if (isFireImmuneMorph(type)) {
            addEffect(player, MobEffects.FIRE_RESISTANCE);
        }
    }

    public static void stripPassives(ServerPlayer player) {
        player.setData(MorphAttachments.MORPH_ACTIVE_EFFECTS, new HashSet<>());
        MANAGED_EFFECTS.forEach(player::removeEffect);
        removeModifier(player, Attributes.ARMOR, MORPH_ARMOR_ID);
        removeModifier(player, Attributes.JUMP_STRENGTH, MORPH_JUMP_ID);
        removeModifier(player, Attributes.WATER_MOVEMENT_EFFICIENCY, MORPH_SWIM_ID);
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Set<ResourceLocation> active = player.getData(MorphAttachments.MORPH_ACTIVE_EFFECTS);
        if (active.isEmpty()) return;
        ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(event.getEffect().value());
        if (active.contains(effectId)) event.setCanceled(true);
    }

    public static void handleFlutter(ChickenFlutterPayload payload, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        player.getExistingData(MorphAttachments.ACTIVE_MORPH)
            .flatMap(opt -> opt)
            .ifPresent(type -> {
                if (type != EntityType.CHICKEN) return;
                if (payload.fluttering()) {
                    addEffect(player, MobEffects.SLOW_FALLING);
                } else {
                    player.removeEffect(MobEffects.SLOW_FALLING);
                }
            });
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        player.getExistingData(MorphAttachments.ACTIVE_MORPH)
            .flatMap(opt -> opt)
            .ifPresent(type -> {
                if (type == EntityType.CHICKEN) event.setCanceled(true);
            });
    }

    private static boolean isFireImmuneMorph(EntityType<?> type) {
        return type == EntityType.ZOMBIFIED_PIGLIN
            || type == EntityType.PIGLIN
            || type == EntityType.BLAZE
            || type == EntityType.GHAST
            || type == EntityType.MAGMA_CUBE;
    }

    private static void addEffect(ServerPlayer player, Holder<MobEffect> effect) {
        player.addEffect(new MobEffectInstance(effect, Integer.MAX_VALUE, 0, false, false, false));
        ResourceLocation effectId = BuiltInRegistries.MOB_EFFECT.getKey(effect.value());
        Set<ResourceLocation> active = player.getData(MorphAttachments.MORPH_ACTIVE_EFFECTS);
        active.add(effectId);
        player.setData(MorphAttachments.MORPH_ACTIVE_EFFECTS, active);
    }

    private static void addModifier(ServerPlayer player, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, ResourceLocation id, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
    }

    private static void removeModifier(ServerPlayer player, Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, ResourceLocation id) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) instance.removeModifier(id);
    }
}
