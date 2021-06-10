package me.drex.itsours.gui;

import net.minecraft.entity.player.PlayerEntity;

public class ClaimListScreenHandler extends PagedScreenHandler {

    protected ClaimListScreenHandler(int syncId, int rows, PlayerEntity player) {
        super(syncId, rows, player);
    }

    @Override
    public int getMaxPage() {
        return 0;
    }
}
