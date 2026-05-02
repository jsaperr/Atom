package com.jsaperr.atom.client;

import com.jsaperr.atom.Atom;
import com.jsaperr.atom.mixin.accessor.ChickenAccessor;
import com.jsaperr.atom.morph.ChickenFlutterPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Atom.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class MorphPuppetManager {
    private static final Map<UUID, LivingEntity> puppets = new HashMap<>();
    private static boolean lastFluttering = false;

    public static void setMorph(UUID playerUuid, Optional<EntityType<?>> morphType, CompoundTag variantTag) {
        if (morphType.isEmpty()) {
            puppets.remove(playerUuid);
            return;
        }
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        var entity = morphType.get().create(level);
        if (entity instanceof LivingEntity living) {
            if (!variantTag.isEmpty()) {
                living.load(variantTag);
                living.setHealth(living.getMaxHealth());
            }
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

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        LivingEntity puppet = puppets.get(mc.player.getUUID());

        boolean isChicken = puppet instanceof Chicken;
        boolean fluttering = isChicken && !mc.player.onGround() && mc.options.keyJump.isDown();

        if (fluttering != lastFluttering) {
            PacketDistributor.sendToServer(new ChickenFlutterPayload(fluttering));
            lastFluttering = fluttering;
        }

        if (!isChicken) return;
        Chicken chicken = (Chicken) puppet;
        ChickenAccessor acc = (ChickenAccessor) chicken;
        acc.setOFlap(acc.getFlap());
        acc.setOFlapSpeed(acc.getFlapSpeed());
        float speed = acc.getFlapSpeed() + (fluttering ? 1.2f : -0.3f);
        speed = Mth.clamp(speed, 0.0f, 1.0f);
        if (fluttering && speed < 0.5f) speed = 0.5f;
        acc.setFlapSpeed(speed);
        acc.setFlap(acc.getFlap() + speed * 2.0f);
    }
}
