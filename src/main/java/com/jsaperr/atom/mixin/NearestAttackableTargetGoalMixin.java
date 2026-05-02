package com.jsaperr.atom.mixin;

import com.jsaperr.atom.MorphCategories;
import com.jsaperr.atom.morph.MorphAttachments;
import com.jsaperr.atom.mixin.accessor.TargetGoalAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(NearestAttackableTargetGoal.class)
public class NearestAttackableTargetGoalMixin {
    @Shadow @Nullable protected LivingEntity target;

    @Inject(method = "canUse", at = @At("RETURN"), cancellable = true)
    private void onCanUse(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        if (!(((TargetGoalAccessor)(Object)this).getMob() instanceof Enemy)) return;
        if (!(this.target instanceof Player player)) return;
        player.getExistingData(MorphAttachments.ACTIVE_MORPH)
            .flatMap(opt -> opt)
            .filter(MorphCategories::isHostileMorph)
            .ifPresent(type -> {
                this.target = null;
                cir.setReturnValue(false);
            });
    }
}
