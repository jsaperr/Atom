package com.jsaperr.atom.mixin;

import com.jsaperr.atom.morph.MorphAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mixin(LivingEntity.class)
public class SpiderClimbMixin {
    @Shadow private Optional<BlockPos> lastClimbablePos;

    private static final Set<UUID> WALL_GRABBING = new HashSet<>();

    @Inject(method = "onClimbable", at = @At("RETURN"), cancellable = true)
    private void onSpiderClimb(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof Player player)) return;

        player.getExistingData(MorphAttachments.ACTIVE_MORPH)
            .flatMap(opt -> opt)
            .ifPresent(type -> {
                if (type != EntityType.SPIDER && type != EntityType.CAVE_SPIDER) return;
                if (player.onGround()) {
                    WALL_GRABBING.remove(player.getUUID());
                    return;
                }
                if (self.horizontalCollision) {
                    WALL_GRABBING.add(player.getUUID());
                }
                if (WALL_GRABBING.contains(player.getUUID())) {
                    lastClimbablePos = Optional.of(self.blockPosition());
                    cir.setReturnValue(true);
                }
            });
    }
}
