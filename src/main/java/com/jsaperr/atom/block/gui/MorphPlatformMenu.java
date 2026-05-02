package com.jsaperr.atom.block.gui;

import com.jsaperr.atom.Atom;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class MorphPlatformMenu extends AbstractContainerMenu {

    public MorphPlatformMenu(int containerId, Inventory playerInventory) {
        super(Atom.MORPH_PLATFORM_MENU.get(), containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
