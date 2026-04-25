package com.jsaperr.atom.client;

import com.jsaperr.atom.Atom;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Atom.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class MorphPuppetManager {
    private static final Map<UUID, LivingEntity> puppets = new HashMap<>();

    public static void setMorph(UUID playerUuid, Optional<EntityType<?>> morphType) {
        if (morphType.isEmpty()) {
            puppets.remove(playerUuid);
            return;
        }
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        var entity = morphType.get().create(level);
        if (entity instanceof LivingEntity living) {
            puppets.put(playerUuid, living);
        }
    }

    public static Optional<LivingEntity> getPuppet(UUID playerUuid) {
        return Optional.ofNullable(puppets.get(playerUuid));
    }

    public static void clear() {
        puppets.clear();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        puppets.values().forEach(puppet -> puppet.tickCount++);
    }
}
