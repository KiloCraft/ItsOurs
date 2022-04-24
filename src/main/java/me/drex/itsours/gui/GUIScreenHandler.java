package me.drex.itsours.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerSyncHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class GUIScreenHandler extends ScreenHandler {

    protected final int rows;
    protected final ServerPlayerEntity player;
    protected ScreenHandlerSyncHandler syncHandler;
    protected GUIScreenHandler previous;
    protected final Inventory inventory;

    protected GUIScreenHandler(int syncId, int rows, ServerPlayerEntity player) {
        super(toScreenHandlerType(rows), syncId);
        this.rows = rows;
        this.player = player;
        this.inventory = new SimpleInventory(rows * 9);
        int i = (rows - 4) * 18;
        for (int j = 0; j < rows; ++j) {
            for (int k = 0; k < 9; ++k) {
                this.addSlot(new Slot(this.inventory, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        for (int l = 0; l < 3; ++l) {
            for (int m = 0; m < 9; ++m) {
                this.addSlot(new Slot(player.getInventory(), m + l * 9 + 9, 8 + m * 18, 103 + l * 18 + i));
            }
        }

        for (int n = 0; n < 9; ++n) {
            this.addSlot(new Slot(player.getInventory(), n, 8 + n * 18, 161 + i));
        }
    }

    private static ScreenHandlerType<? extends ScreenHandler> toScreenHandlerType(int rows) {
        return switch (rows) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            case 6 -> ScreenHandlerType.GENERIC_9X6;
            default -> throw new RuntimeException("Invalid row count: " + rows);
        };
    }

    protected abstract void fillInventory(PlayerEntity player, Inventory inv);

    public void fillEmpty() {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).isEmpty()) {
                inventory.setStack(i, new ItemStack(Items.GRAY_STAINED_GLASS_PANE));
            }
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    /*public void addBack() {
        ItemStack back = ScreenHelper.createPlayerHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY1MmUyYjkzNmNhODAyNmJkMjg2NTFkN2M5ZjI4MTlkMmU5MjM2OTc3MzRkMThkZmRiMTM1NTBmOGZkYWQ1ZiJ9fX0=", UUID.fromString("e8627b92-1dcb-4733-810c-a2b47833c451"));
        ScreenHelper.setCustomName(back, Component.text("Back").color(NamedTextColor.GRAY));
        inventory.setStack(0, back);
    }*/

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasStack()) {
            ItemStack slotStack = slot.getStack();
            itemStack = slotStack.copy();
            this.handleSlotClick((ServerPlayerEntity) player, index, slot, true, true);
            this.sendContentUpdates();
        }
        return itemStack;
    }

    @Override
    public void updateSyncHandler(ScreenHandlerSyncHandler handler) {
        super.updateSyncHandler(handler);
        this.syncHandler = handler;
    }

    @Override
    public void onSlotClick(int i, int j, SlotActionType actionType, PlayerEntity playerEntity) {
        if (i < 0) return;
        Slot slot = this.slots.get(i);
        if (this.syncHandler != null) this.syncHandler.updateCursorStack(this, this.getCursorStack().copy());
        this.handleSlotClick((ServerPlayerEntity) playerEntity, i, slot, j == 0, false);
    }

    protected abstract void handleSlotClick(ServerPlayerEntity player, int index, Slot slot, boolean leftClick, boolean shift);

}
