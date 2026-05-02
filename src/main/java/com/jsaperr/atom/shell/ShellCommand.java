package com.jsaperr.atom.shell;

import com.jsaperr.atom.Atom;
import com.jsaperr.atom.morph.MorphAttachments;
import com.jsaperr.atom.morph.MorphSyncPayload;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = Atom.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ShellCommand {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("shell")
                .then(Commands.literal("save").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    ShellPosition pos = new ShellPosition(
                            player.level().dimension().location(),
                            player.getX(), player.getY(), player.getZ(),
                            player.getYRot(), player.getXRot()
                    );
                    Optional<ResourceLocation> morph = player.getExistingData(MorphAttachments.ACTIVE_MORPH)
                            .flatMap(opt -> opt)
                            .map(type -> net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(type));
                    ShellState shell = new ShellState(
                            UUID.randomUUID(),
                            pos,
                            player.getHealth(),
                            player.getFoodData().getFoodLevel(),
                            player.getFoodData().getSaturationLevel(),
                            player.getFoodData().getExhaustionLevel(),
                            player.experienceLevel,
                            player.experienceProgress,
                            ShellState.snapshotInventory(player.getInventory()),
                            morph
                    );
                    List<ShellState> shells = new ArrayList<>(player.getData(ShellAttachments.SHELLS));
                    shells.add(shell);
                    player.setData(ShellAttachments.SHELLS, shells);

                    ShellEntity shellEntity = new ShellEntity(
                            Atom.SHELL_ENTITY.get(), player.serverLevel(),
                            shell, player.getUUID(), player.getGameProfile().getName()
                    );
                    shellEntity.setPos(player.getX(), player.getY(), player.getZ());
                    shellEntity.setYRot(player.getYRot());
                    shellEntity.setXRot(player.getXRot());
                    boolean added = player.serverLevel().addFreshEntity(shellEntity);
                    net.minecraft.server.MinecraftServer server = player.getServer();
                    if (server != null) {
                        server.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "[Shell] addFreshEntity=" + added
                            + " id=" + shellEntity.getId()
                            + " pos=" + String.format("%.1f %.1f %.1f", shellEntity.getX(), shellEntity.getY(), shellEntity.getZ())
                            + " removed=" + shellEntity.isRemoved()
                        ));
                    }

                    ctx.getSource().sendSuccess(() -> Component.literal("Shell saved: " + shell.uuid()), false);
                    return 1;
                }))
                .then(Commands.literal("list").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    List<ShellState> shells = player.getData(ShellAttachments.SHELLS);
                    if (shells.isEmpty()) {
                        ctx.getSource().sendSuccess(() -> Component.literal("No shells saved."), false);
                        return 1;
                    }
                    for (int i = 0; i < shells.size(); i++) {
                        ShellState s = shells.get(i);
                        int idx = i;
                        ctx.getSource().sendSuccess(() -> Component.literal(
                                "[" + idx + "] " + s.position().dimension() +
                                " | " + String.format("%.1f %.1f %.1f", s.position().x(), s.position().y(), s.position().z()) +
                                " | " + String.format("%.1f", s.health()) + " hp"
                        ), false);
                    }
                    return 1;
                }))
                .then(Commands.literal("sync")
                    .then(Commands.argument("index", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            int index = IntegerArgumentType.getInteger(ctx, "index");
                            List<ShellState> shells = new ArrayList<>(player.getData(ShellAttachments.SHELLS));
                            if (index >= shells.size()) {
                                ctx.getSource().sendFailure(Component.literal("No shell at index " + index + "."));
                                return 0;
                            }
                            ShellState target = shells.get(index);
                            applyShell(player, target);
                            shells.remove(index);
                            player.setData(ShellAttachments.SHELLS, shells);
                            ctx.getSource().sendSuccess(() -> Component.literal("Synced into shell."), false);
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("remove")
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        List<ShellState> shells = new ArrayList<>(player.getData(ShellAttachments.SHELLS));
                        if (shells.isEmpty()) {
                            ctx.getSource().sendSuccess(() -> Component.literal("No shells to remove."), false);
                            return 1;
                        }
                        shells.forEach(s -> {
                            var entity = player.serverLevel().getEntity(s.uuid());
                            if (entity != null) entity.discard();
                        });
                        int count = shells.size();
                        player.setData(ShellAttachments.SHELLS, new ArrayList<>());
                        ctx.getSource().sendSuccess(() -> Component.literal("Removed " + count + " shell(s)."), false);
                        return 1;
                    })
                    .then(Commands.argument("index", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            int index = IntegerArgumentType.getInteger(ctx, "index");
                            List<ShellState> shells = new ArrayList<>(player.getData(ShellAttachments.SHELLS));
                            if (index >= shells.size()) {
                                ctx.getSource().sendFailure(Component.literal("No shell at index " + index + "."));
                                return 0;
                            }
                            ShellState target = shells.get(index);
                            var entity = player.serverLevel().getEntity(target.uuid());
                            if (entity != null) entity.discard();
                            shells.remove(index);
                            player.setData(ShellAttachments.SHELLS, shells);
                            ctx.getSource().sendSuccess(() -> Component.literal("Removed shell " + index + "."), false);
                            return 1;
                        })
                    )
                )
        );
    }

    private static void applyShell(ServerPlayer player, ShellState state) {
        var shellEntity = player.serverLevel().getEntity(state.uuid());
        if (shellEntity != null) shellEntity.discard();
        player.setHealth(state.health());
        player.getFoodData().setFoodLevel(state.foodLevel());
        player.getFoodData().setSaturation(state.saturation());
        player.getFoodData().setExhaustion(state.exhaustion());
        player.experienceLevel = state.xpLevel();
        player.experienceProgress = state.xpProgress();
        player.getInventory().clearContent();
        player.getInventory().load(state.inventoryNbt().getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND));
        Optional<net.minecraft.world.entity.EntityType<?>> morphType = state.morph()
                .flatMap(id -> BuiltInRegistries.ENTITY_TYPE.getOptional(id));
        net.minecraft.nbt.CompoundTag variantTag = morphType
                .map(t -> com.jsaperr.atom.morph.MorphVariantHelper.randomVariantTag(t, player.serverLevel(), player.blockPosition()))
                .orElse(new net.minecraft.nbt.CompoundTag());
        player.setData(MorphAttachments.ACTIVE_MORPH, morphType);
        player.setData(com.jsaperr.atom.morph.MorphAttachments.ACTIVE_MORPH_VARIANT, variantTag);
        player.refreshDimensions();
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new MorphSyncPayload(player.getUUID(), state.morph(), variantTag));
        ShellPosition pos = state.position();
        ServerLevel targetLevel = player.getServer().getLevel(
                ResourceKey.create(Registries.DIMENSION, pos.dimension())
        );
        if (targetLevel == null) targetLevel = player.getServer().overworld();
        player.teleportTo(targetLevel, pos.x(), pos.y(), pos.z(), pos.yRot(), pos.xRot());
    }
}
