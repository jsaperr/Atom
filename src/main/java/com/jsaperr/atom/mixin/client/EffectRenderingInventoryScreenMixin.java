package com.jsaperr.atom.mixin.client;

import com.jsaperr.atom.client.MorphPuppetManager;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Mixin(EffectRenderingInventoryScreen.class)
public class EffectRenderingInventoryScreenMixin {

    @Redirect(method = "renderEffects", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/player/LocalPlayer;getActiveEffects()Ljava/util/Collection;"))
    private Collection<MobEffectInstance> filterMorphEffects(LocalPlayer player) {
        Collection<MobEffectInstance> effects = player.getActiveEffects();
        return MorphPuppetManager.getPuppet(player.getUUID())
            .map(puppet -> {
                Set<ResourceLocation> hidden = getHiddenEffects(puppet.getType());
                if (hidden.isEmpty()) return effects;
                return (Collection<MobEffectInstance>) effects.stream()
                    .filter(e -> !hidden.contains(BuiltInRegistries.MOB_EFFECT.getKey(e.getEffect().value())))
                    .toList();
            })
            .orElse(effects);
    }

    private static Set<ResourceLocation> getHiddenEffects(EntityType<?> type) {
        Set<ResourceLocation> set = new HashSet<>();
        if (type == EntityType.FOX) {
            set.add(key(MobEffects.NIGHT_VISION));
        } else if (isFireImmuneMorph(type)) {
            set.add(key(MobEffects.FIRE_RESISTANCE));
        }
        if (type == EntityType.CHICKEN) {
            set.add(key(MobEffects.SLOW_FALLING));
        }
        return set;
    }

    private static ResourceLocation key(Holder<MobEffect> effect) {
        return BuiltInRegistries.MOB_EFFECT.getKey(effect.value());
    }

    private static boolean isFireImmuneMorph(EntityType<?> type) {
        return type == EntityType.ZOMBIFIED_PIGLIN
            || type == EntityType.PIGLIN
            || type == EntityType.BLAZE
            || type == EntityType.GHAST
            || type == EntityType.MAGMA_CUBE;
    }
}
