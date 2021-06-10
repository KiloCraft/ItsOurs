package me.drex.itsours.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public abstract class PagedScreenHandler extends GUIScreenHandler {

    protected int page = 0;

    protected PagedScreenHandler(int syncId, int rows, PlayerEntity player) {
        super(syncId, rows, player);
    }

    @Override
    protected void fillInventory(PlayerEntity player, Inventory inv) {
        if (getMaxPage() > page) {
            ItemStack nextPage = new ItemStack(Items.ARROW);
            nextPage.setCustomName(new LiteralText("Next page").formatted(Formatting.GRAY));
            inventory.setStack(53, nextPage);
        }
        if (page > 0) {
            ItemStack nextPage = new ItemStack(Items.ARROW);
            nextPage.setCustomName(new LiteralText("Previous page").formatted(Formatting.GRAY));
            inventory.setStack(45, nextPage);
        }
    }

    public abstract int getMaxPage();

    @Override
    protected void handleSlotClick(ServerPlayerEntity player, int index, Slot slot, boolean leftClick, boolean shift) {
        switch (index) {
            case 45 -> {
                if (page > 0) {
                    page--;
                    fillInventory(player, inventory);
                }
            }
            case 53 -> {
                if (getMaxPage() > page) {
                    page++;
                    fillInventory(player, inventory);
                }
            }
        }


    }
}
