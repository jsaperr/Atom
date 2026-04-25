package com.jsaperr.atom.mixin.accessor;

import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WalkAnimationState.class)
public interface WalkAnimationStateAccessor {
    @Accessor float getPosition();
    @Accessor float getSpeed();
    @Accessor float getSpeedOld();
    @Accessor void setPosition(float position);
    @Accessor void setSpeedOld(float speedOld);
}
