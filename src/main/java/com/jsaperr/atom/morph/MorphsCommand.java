package com.jsaperr.atom.morph;

import com.jsaperr.atom.Atom;
import com.jsaperr.atom.MorphCategories;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = Atom.MODID, bus = EventBusSubscriber.Bus.GAME)
public class MorphsCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
            Commands.literal("morphs")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("unlock")
                    .then(Commands.argument("category", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("all");
                            MorphCategories.CATEGORIES.keySet()
                                .forEach(rl -> builder.suggest(rl.getPath()));
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String cat = StringArgumentType.getString(ctx, "category");
                            return unlock(ctx.getSource(), player, cat);
                        })
                    )
                )
                .then(Commands.literal("remove")
                    .then(Commands.argument("category", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("all");
                            MorphCategories.CATEGORIES.keySet()
                                .forEach(rl -> builder.suggest(rl.getPath()));
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            String cat = StringArgumentType.getString(ctx, "category");
                            return remove(ctx.getSource(), player, cat);
                        })
                    )
                )
        );
    }

    private static int unlock(CommandSourceStack src, ServerPlayer player, String cat) {
        Set<ResourceLocation> unlocked = new HashSet<>(player.getData(MorphUnlockAttachments.UNLOCKED_CATEGORIES));
        if (cat.equals("all")) {
            MorphCategories.CATEGORIES.keySet().forEach(unlocked::add);
            player.setData(MorphUnlockAttachments.UNLOCKED_CATEGORIES, unlocked);
            src.sendSuccess(() -> Component.literal("Unlocked all morph categories"), false);
            return 1;
        }
        ResourceLocation catId = ResourceLocation.fromNamespaceAndPath("atom", cat);
        if (!MorphCategories.CATEGORIES.containsKey(catId)) {
            src.sendFailure(Component.literal("Unknown category: " + cat
                    + ". Valid: passive, common_hostile, nether"));
            return 0;
        }
        unlocked.add(catId);
        player.setData(MorphUnlockAttachments.UNLOCKED_CATEGORIES, unlocked);
        src.sendSuccess(() -> Component.literal("Unlocked category: " + cat), false);
        return 1;
    }

    private static int remove(CommandSourceStack src, ServerPlayer player, String cat) {
        Set<ResourceLocation> unlocked = new HashSet<>(player.getData(MorphUnlockAttachments.UNLOCKED_CATEGORIES));
        if (cat.equals("all")) {
            unlocked.clear();
            player.setData(MorphUnlockAttachments.UNLOCKED_CATEGORIES, unlocked);
            src.sendSuccess(() -> Component.literal("Removed all morph categories"), false);
            return 1;
        }
        ResourceLocation catId = ResourceLocation.fromNamespaceAndPath("atom", cat);
        if (!MorphCategories.CATEGORIES.containsKey(catId)) {
            src.sendFailure(Component.literal("Unknown category: " + cat
                    + ". Valid: passive, common_hostile, nether"));
            return 0;
        }
        unlocked.remove(catId);
        player.setData(MorphUnlockAttachments.UNLOCKED_CATEGORIES, unlocked);
        src.sendSuccess(() -> Component.literal("Removed category: " + cat), false);
        return 1;
    }
}
