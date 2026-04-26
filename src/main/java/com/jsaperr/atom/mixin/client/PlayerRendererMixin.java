package com.jsaperr.atom.mixin.client;

import com.jsaperr.atom.client.MorphPuppetManager;
import com.jsaperr.atom.mixin.accessor.WalkAnimationStateAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {
    @Inject(method = "renderRightHand", at = @At("HEAD"), cancellable = true)
    private void onRenderRightHand(PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
                                   AbstractClientPlayer player, CallbackInfo ci) {
        if (MorphPuppetManager.getPuppet(player.getUUID()).isPresent()) ci.cancel();
    }

    @Inject(method = "renderLeftHand", at = @At("HEAD"), cancellable = true)
    private void onRenderLeftHand(PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
                                  AbstractClientPlayer player, CallbackInfo ci) {
        if (MorphPuppetManager.getPuppet(player.getUUID()).isPresent()) ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(AbstractClientPlayer player, float entityYaw, float partialTick,
                          PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        MorphPuppetManager.getPuppet(player.getUUID()).ifPresent(puppet -> {
            puppet.setPos(player.getX(), player.getY(), player.getZ());
            puppet.setYRot(player.getYRot());
            puppet.setXRot(player.getXRot());
            puppet.yRotO = player.yRotO;
            puppet.xRotO = player.xRotO;
            puppet.yBodyRot = player.yBodyRot;
            puppet.yBodyRotO = player.yBodyRotO;
            puppet.yHeadRot = player.yHeadRot;
            puppet.yHeadRotO = player.yHeadRotO;
            var src = (WalkAnimationStateAccessor) player.walkAnimation;
            var dst = (WalkAnimationStateAccessor) puppet.walkAnimation;
            dst.setPosition(src.getPosition());
            puppet.walkAnimation.setSpeed(src.getSpeed());
            dst.setSpeedOld(src.getSpeedOld());

            @SuppressWarnings("unchecked")
            EntityRenderer<LivingEntity> renderer =
                (EntityRenderer<LivingEntity>) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(puppet);
            renderer.render(puppet, entityYaw, partialTick, poseStack, buffer, packedLight);
            ci.cancel();
        });
    }
}
