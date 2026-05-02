package com.jsaperr.atom.block;

import com.jsaperr.atom.Atom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ResearchStationBlockEntity extends BlockEntity {

    private final Set<ResourceLocation> researched = new HashSet<>();

    public ResearchStationBlockEntity(BlockPos pos, BlockState state) {
        super(Atom.RESEARCH_STATION_BE.get(), pos, state);
    }

    /** Returns true if this was a new entry (extractor should be consumed). */
    public boolean research(ResourceLocation entityTypeId) {
        if (entityTypeId == null) return false;
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(entityTypeId)) return false;
        boolean added = researched.add(entityTypeId);
        if (added) setChanged();
        return added;
    }

    public boolean hasResearched(ResourceLocation entityTypeId) {
        return researched.contains(entityTypeId);
    }

    public Set<ResourceLocation> getResearched() {
        return Collections.unmodifiableSet(researched);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (ResourceLocation id : researched) {
            list.add(StringTag.valueOf(id.toString()));
        }
        tag.put("researched", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        researched.clear();
        ListTag list = tag.getList("researched", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) researched.add(id);
        }
    }
}
