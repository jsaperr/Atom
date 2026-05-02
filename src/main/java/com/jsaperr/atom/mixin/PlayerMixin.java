package com.jsaperr.atom.mixin;

import com.jsaperr.atom.morph.MorphAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class PlayerMixin {
    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void onGetDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (!(((Object)this) instanceof Player self)) return;
        self.getExistingData(MorphAttachments.ACTIVE_MORPH)
            .flatMap(opt -> opt)
            .ifPresent(type -> {
                EntityDimensions dims;
                if (type == EntityType.SLIME || type == EntityType.MAGMA_CUBE) {
                    CompoundTag tag = self.getData(MorphAttachments.ACTIVE_MORPH_VARIANT);
                    int size = Math.max(1, tag.getInt("Size") + 1);
                    dims = type.getDimensions().scale(size);
                } else {
                    dims = type.getDimensions();
                }
                cir.setReturnValue(dims);
            });
    }
}
