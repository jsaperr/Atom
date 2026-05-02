package com.jsaperr.atom.block;

import com.jsaperr.atom.MorphCategories;
import com.jsaperr.atom.item.ExtractorItem;
import com.jsaperr.atom.morph.MorphUnlockAttachments;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;

import com.jsaperr.atom.block.gui.ResearchStationMenu;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResearchStationBlock extends BaseEntityBlock {

    public static final MapCodec<ResearchStationBlock> CODEC = simpleCodec(ResearchStationBlock::new);

    public ResearchStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResearchStationBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof ResearchStationBlockEntity be)) return InteractionResult.FAIL;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;

        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof ExtractorItem && ExtractorItem.isLoaded(held)) {
            boolean added = be.research(ExtractorItem.getStoredType(held));
            if (added) {
                ExtractorItem.clearStoredType(held);
                player.setItemInHand(InteractionHand.MAIN_HAND, held);
                PacketDistributor.sendToPlayer(serverPlayer,
                        new ResearchSyncPayload(pos, be.getResearched()));
                checkCategoryCompletion(serverPlayer, be.getResearched());
            }
            return InteractionResult.SUCCESS;
        }

        MenuProvider menuProvider = new SimpleMenuProvider(
                (id, inv, p) -> new ResearchStationMenu(id, inv),
                Component.translatable("block.atom.research_station")
        );
        serverPlayer.openMenu(menuProvider);
        PacketDistributor.sendToPlayer(serverPlayer,
                new ResearchSyncPayload(pos, be.getResearched()));

        return InteractionResult.SUCCESS;
    }

    private static void checkCategoryCompletion(ServerPlayer player, Set<ResourceLocation> researched) {
        Set<ResourceLocation> unlocked = new HashSet<>(player.getData(MorphUnlockAttachments.UNLOCKED_CATEGORIES));
        boolean changed = false;
        for (Map.Entry<ResourceLocation, List<ResourceLocation>> entry : MorphCategories.CATEGORIES.entrySet()) {
            ResourceLocation categoryId = entry.getKey();
            if (unlocked.contains(categoryId)) continue;
            if (researched.containsAll(entry.getValue())) {
                unlocked.add(categoryId);
                changed = true;
                String name = categoryId.getPath().replace("_", " ");
                name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                player.sendSystemMessage(Component.literal("Category unlocked: " + name));
            }
        }
        if (changed) {
            player.setData(MorphUnlockAttachments.UNLOCKED_CATEGORIES, unlocked);
        }
    }
}
