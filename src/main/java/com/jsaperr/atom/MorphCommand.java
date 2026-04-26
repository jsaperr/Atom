package com.jsaperr.atom;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

@EventBusSubscriber(modid = Atom.MODID, bus = EventBusSubscriber.Bus.GAME)
public class MorphCommand {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
            Commands.literal("morph")
                .then(Commands.argument("entity_type", ResourceLocationArgument.id())
                    .suggests((ctx, builder) -> {
                        BuiltInRegistries.ENTITY_TYPE.keySet()
                            .forEach(rl -> builder.suggest(rl.toString()));
                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        ResourceLocation id = ResourceLocationArgument.getId(ctx, "entity_type");
                        Optional<EntityType<?>> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
                        if (entityType.isEmpty()) {
                            ctx.getSource().sendFailure(Component.literal("Unknown entity type: " + id));
                            return 0;
                        }
                        player.setData(MorphAttachments.ACTIVE_MORPH, entityType);
                        player.refreshDimensions();
                        applyStepHeight(player, entityType.get());
                        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                            new MorphSyncPayload(player.getUUID(), Optional.of(id)));
                        ctx.getSource().sendSuccess(() -> Component.literal("Morphed into " + id), false);
                        return 1;
                    })
                )
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    player.setData(MorphAttachments.ACTIVE_MORPH, Optional.empty());
                    player.refreshDimensions();
                    resetStepHeight(player);
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                        new MorphSyncPayload(player.getUUID(), Optional.empty()));
                    ctx.getSource().sendSuccess(() -> Component.literal("Returned to normal form"), false);
                    return 1;
                })
        );
    }

    public static void applyStepHeight(ServerPlayer player, EntityType<?> type) {
        var attr = player.getAttribute(Attributes.STEP_HEIGHT);
        if (attr == null) return;
        if (DefaultAttributes.hasSupplier(type)) {
            var supplier = DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) type);
            double value = supplier.hasAttribute(Attributes.STEP_HEIGHT)
                ? supplier.getBaseValue(Attributes.STEP_HEIGHT)
                : 0.6;
            attr.setBaseValue(value);
        }
    }

    public static void resetStepHeight(ServerPlayer player) {
        var attr = player.getAttribute(Attributes.STEP_HEIGHT);
        if (attr != null) attr.setBaseValue(0.6);
    }
}
