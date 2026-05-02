package com.jsaperr.atom.mixin.accessor;

import net.minecraft.world.entity.animal.Chicken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Chicken.class)
public interface ChickenAccessor {
    @Accessor float getFlap();
    @Accessor void setFlap(float v);
    @Accessor float getOFlap();
    @Accessor void setOFlap(float v);
    @Accessor float getFlapSpeed();
    @Accessor void setFlapSpeed(float v);
    @Accessor float getOFlapSpeed();
    @Accessor void setOFlapSpeed(float v);
}
