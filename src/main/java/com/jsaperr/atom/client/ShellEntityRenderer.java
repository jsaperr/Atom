package com.jsaperr.atom.client;

import com.jsaperr.atom.shell.ShellEntity;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ShellEntityRenderer extends EntityRenderer<ShellEntity> {
    @SuppressWarnings("rawtypes")
    private final PlayerModel wideModel;
    @SuppressWarnings("rawtypes")
    private final PlayerModel slimModel;
    private final Map<UUID, Supplier<PlayerSkin>> skinCache = new HashMap<>();

    public ShellEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.wideModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        this.slimModel = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    private static int debugTick = 0;

    @Override
    public void render(ShellEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (debugTick++ % 100 == 0) {
            com.mojang.logging.LogUtils.getLogger().info(
                "[ShellRenderer] render called entity={} morph={} pos={} {} {}",
                entity.getId(), entity.getMorph(), entity.getX(), entity.getY(), entity.getZ()
            );
        }
        entity.getMorph().ifPresentOrElse(
                morphId -> renderMorphed(entity, morphId, yaw, partialTick, poseStack, buffer, packedLight),
                () -> renderPlayerModel(entity, poseStack, buffer, packedLight)
        );
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private PlayerSkin resolveSkin(ShellEntity entity) {
        var level = Minecraft.getInstance().level;
        var ownerUuid = entity.getOwnerUuid();
        if (level != null && ownerUuid.isPresent()) {
            var player = level.getPlayerByUUID(ownerUuid.get());
            if (player instanceof AbstractClientPlayer clientPlayer) {
                return clientPlayer.getSkin();
            }
        }
        GameProfile profile = new GameProfile(ownerUuid.orElse(new UUID(0, 0)), entity.getOwnerName());
        return skinCache.computeIfAbsent(
                ownerUuid.orElse(new UUID(0, 0)),
                id -> Minecraft.getInstance().getSkinManager().lookupInsecure(profile)
        ).get();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void renderPlayerModel(ShellEntity entity, PoseStack poseStack,
                                   MultiBufferSource buffer, int packedLight) {
        var skin = resolveSkin(entity);
        boolean slim = skin.model() == PlayerSkin.Model.SLIM;
        PlayerModel model = slim ? slimModel : wideModel;
        model.young = false;
        model.head.xRot = entity.getXRot() * (float)(Math.PI / 180.0);
        model.hat.copyFrom(model.head);
        ResourceLocation texture = skin.texture();

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - entity.getYRot()));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(0.0f, -1.501f, 0.0f);
        model.renderToBuffer(poseStack, buffer.getBuffer(model.renderType(texture)), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderMorphed(ShellEntity entity, ResourceLocation morphId, float yaw, float partialTick,
                               PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(morphId);
        if (type == null) return;
        LivingEntity puppet = (LivingEntity) type.create(Minecraft.getInstance().level);
        if (puppet == null) return;

        puppet.setPos(entity.getX(), entity.getY(), entity.getZ());
        puppet.setYRot(entity.getYRot());
        puppet.yBodyRot = entity.getYRot();
        puppet.yHeadRot = entity.getYRot();

        @SuppressWarnings("unchecked")
        var renderer = (EntityRenderer<LivingEntity>)
                Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(puppet);
        renderer.render(puppet, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ShellEntity entity) {
        return entity.getOwnerUuid()
                .map(DefaultPlayerSkin::get)
                .map(PlayerSkin::texture)
                .orElse(DefaultPlayerSkin.getDefaultTexture());
    }
}
