package me.drex.itsours.gui.screen;


import me.drex.itsours.gui.util.ScreenHelper;
import me.drex.itsours.gui.util.SlotEntry;
import me.drex.itsours.gui.util.context.NoContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class BackScreen<K extends NoContext> extends SimpleScreen<K> {

    protected SimpleScreen<?> previous;

    @Override
    public void draw() {
        SlotEntry<K> slotEntry;
        if (previous != null) {
            ItemStack item = ScreenHelper.createPlayerHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY1MmUyYjkzNmNhODAyNmJkMjg2NTFkN2M5ZjI4MTlkMmU5MjM2OTc3MzRkMThkZmRiMTM1NTBmOGZkYWQ1ZiJ9fX0=", UUID.fromString("e8627b92-1dcb-4733-810c-a2b47833c451"));
            ScreenHelper.setCustomName(item, "Back");
            slotEntry = new SlotEntry<>(item, (noC, leftClick, shiftClick) -> {
                player.closeHandledScreen();
                previous.render();
            });
        } else {
            ItemStack item = ScreenHelper.createPlayerHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ==", UUID.fromString("5ecfabf0-5253-47b0-a44d-9a0c924081b9"));
            ScreenHelper.setCustomName(item,"Close");
            slotEntry = new SlotEntry<>(item, (noC, leftClick, shiftClick) -> {
                player.closeHandledScreen();
            });
        }
        addSlot(slotEntry, 0);
        super.draw();
    }

    public BackScreen(ServerPlayerEntity player, int rows, K context, SimpleScreen<?> previous) {
        super(player, rows, context);
        this.previous = previous;
    }


}
