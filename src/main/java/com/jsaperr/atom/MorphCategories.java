package com.jsaperr.atom;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.*;

public class MorphCategories {

    public static final ResourceLocation PASSIVE        = ResourceLocation.fromNamespaceAndPath("atom", "passive");
    public static final ResourceLocation COMMON_HOSTILE = ResourceLocation.fromNamespaceAndPath("atom", "common_hostile");
    public static final ResourceLocation NETHER         = ResourceLocation.fromNamespaceAndPath("atom", "nether");

    public static final LinkedHashMap<ResourceLocation, List<ResourceLocation>> CATEGORIES = new LinkedHashMap<>();
    private static final Set<ResourceLocation> HOSTILE_MORPH_IDS = new HashSet<>();

    static {
        CATEGORIES.put(PASSIVE, List.of(
                ResourceLocation.withDefaultNamespace("cow"),
                ResourceLocation.withDefaultNamespace("sheep"),
                ResourceLocation.withDefaultNamespace("pig"),
                ResourceLocation.withDefaultNamespace("chicken"),
                ResourceLocation.withDefaultNamespace("rabbit"),
                ResourceLocation.withDefaultNamespace("fox"),
                ResourceLocation.withDefaultNamespace("turtle")
        ));
        CATEGORIES.put(COMMON_HOSTILE, List.of(
                ResourceLocation.withDefaultNamespace("zombie"),
                ResourceLocation.withDefaultNamespace("skeleton"),
                ResourceLocation.withDefaultNamespace("creeper"),
                ResourceLocation.withDefaultNamespace("spider"),
                ResourceLocation.withDefaultNamespace("enderman")
        ));
        CATEGORIES.put(NETHER, List.of(
                ResourceLocation.withDefaultNamespace("zombified_piglin"),
                ResourceLocation.withDefaultNamespace("piglin"),
                ResourceLocation.withDefaultNamespace("blaze"),
                ResourceLocation.withDefaultNamespace("ghast"),
                ResourceLocation.withDefaultNamespace("magma_cube")
        ));

        HOSTILE_MORPH_IDS.addAll(CATEGORIES.get(COMMON_HOSTILE));
        HOSTILE_MORPH_IDS.addAll(CATEGORIES.get(NETHER));
    }

    public static boolean isHostileMorph(EntityType<?> type) {
        return HOSTILE_MORPH_IDS.contains(BuiltInRegistries.ENTITY_TYPE.getKey(type));
    }

    public static List<ResourceLocation> getUnlockedMobs(Set<ResourceLocation> unlockedCategories) {
        List<ResourceLocation> mobs = new ArrayList<>();
        for (Map.Entry<ResourceLocation, List<ResourceLocation>> entry : CATEGORIES.entrySet()) {
            if (unlockedCategories.contains(entry.getKey())) {
                mobs.addAll(entry.getValue());
            }
        }
        return mobs;
    }
}
