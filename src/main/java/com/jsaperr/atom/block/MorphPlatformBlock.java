package com.jsaperr.atom.block;

import com.jsaperr.atom.MorphCategories;
import com.jsaperr.atom.block.gui.MorphPlatformMenu;
import com.jsaperr.atom.morph.MorphUnlockAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Set;

public class MorphPlatformBlock extends Block {

    public MorphPlatformBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.FAIL;

        Set<ResourceLocation> unlocked = serverPlayer.getData(MorphUnlockAttachments.UNLOCKED_CATEGORIES);
        List<ResourceLocation> availableMobs = MorphCategories.getUnlockedMobs(unlocked);

        serverPlayer.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new MorphPlatformMenu(id, inv),
                Component.translatable("block.atom.morph_platform")
        ));
        PacketDistributor.sendToPlayer(serverPlayer, new MorphSelectPayload(availableMobs));

        return InteractionResult.SUCCESS;
    }
}
