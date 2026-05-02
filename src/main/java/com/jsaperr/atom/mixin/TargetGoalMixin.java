package com.jsaperr.atom.mixin;

import com.jsaperr.atom.MorphCategories;
import com.jsaperr.atom.morph.MorphAttachments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetGoal.class)
public class TargetGoalMixin {
    @Shadow @Final protected Mob mob;

    @Inject(method = "canContinueToUse", at = @At("RETURN"), cancellable = true)
    private void onCanContinueToUse(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        if (!(this.mob instanceof Enemy)) return;
        LivingEntity currentTarget = this.mob.getTarget();
        if (!(currentTarget instanceof Player player)) return;
        player.getExistingData(MorphAttachments.ACTIVE_MORPH)
            .flatMap(opt -> opt)
            .filter(MorphCategories::isHostileMorph)
            .ifPresent(type -> cir.setReturnValue(false));
    }
}
