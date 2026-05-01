package com.jsaperr.atom.item;

import com.jsaperr.atom.Atom;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

import java.util.List;

public class ExtractorItem extends Item {

    public static final String TAG_ENTITY_TYPE = "stored_entity_type";

    public ExtractorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide()) return InteractionResult.SUCCESS;
        if (isLoaded(stack)) return InteractionResult.FAIL;

        ResourceLocation entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        if (entityTypeId == null) return InteractionResult.FAIL;

        target.kill();
        setStoredType(stack, entityTypeId);
        player.setItemInHand(hand, stack);
        player.displayClientMessage(
            Component.literal("Extracted: " + entityTypeId.getPath()), true
        );
        return InteractionResult.SUCCESS;
    }

    public static boolean isLoaded(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().contains(TAG_ENTITY_TYPE);
    }

    public static ResourceLocation getStoredType(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        String id = data.copyTag().getString(TAG_ENTITY_TYPE);
        return id.isEmpty() ? null : ResourceLocation.parse(id);
    }

    public static void setStoredType(ItemStack stack, ResourceLocation entityTypeId) {
        CustomData existing = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = existing.copyTag();
        tag.putString(TAG_ENTITY_TYPE, entityTypeId.toString());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void clearStoredType(ItemStack stack) {
        CustomData existing = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = existing.copyTag();
        tag.remove(TAG_ENTITY_TYPE);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public Component getName(ItemStack stack) {
        if (isLoaded(stack)) {
            ResourceLocation id = getStoredType(stack);
            String mobName = id != null ? id.getPath().replace("_", " ") : "unknown";
            return Component.literal("Extractor [" + mobName + "]");
        }
        return Component.literal("Extractor");
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isLoaded(stack);
    }
}
