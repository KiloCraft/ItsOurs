package me.drex.itsours.gui.screen;

import me.drex.itsours.gui.util.SlotEntry;
import me.drex.itsours.gui.util.context.NoContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashSet;
import java.util.Set;

public class PagedScreen<K extends NoContext> extends BackScreen<K> {

    protected int page = 0;
    protected int entries = 0;
    protected int entriesPerPage = 28;
    protected Set<Integer> invalidSlots = new HashSet<>();
    protected int nextSlot = 0;

    public PagedScreen(ServerPlayerEntity player, int rows, K context, SimpleScreen<?> previous) {
        super(player, rows, context, previous);
        for (int i = 0; i < rows; i++) {
            invalidSlots.add(i * 9);
            invalidSlots.add(i * 9 + 8);
        }
        for (int i = 0; i < 9; i++) {
            invalidSlots.add(i);
            invalidSlots.add(i + ((rows - 1) * 9));
        }
    }

    public PagedScreen(ServerPlayerEntity player, int rows, K context) {
        this(player, rows, context, null);
    }

    @Override
    public void draw() {
        SlotEntry<K> previous = new SlotEntry<>(Items.ARROW, "Previous", (NoC, leftClick, shiftClick) -> {
            page--;
            draw();
        });
        addSlot((page > 0) ? previous : fill, (rows - 1) * 9);
        SlotEntry<K> next = new SlotEntry<>(Items.ARROW, "Next", (NoC, leftClick, shiftClick) -> {
            page++;
            draw();
        });
        int maxPage = (entries-1) / entriesPerPage;
        addSlot((page < maxPage) ? next : fill, (rows * 9) - 1);

        SlotEntry<K> current = new SlotEntry<>(Items.OAK_SIGN, "Page " + (page + 1) + " / " + (maxPage + 1));
        addSlot(current, (rows * 9) - 5);
        super.draw();
        nextSlot = 0;
        entries = 0;
    }

    @Override
    public SlotEntry<K> getEntry(int index) {
        if (invalidSlots.contains(index)) {
            return super.getEntry(index);
        } else {
            return data.get(index + (page * rows * 9));
        }
    }

    public void addPageEntry(SlotEntry<K> slotEntry) {
        int i = 0;
        while (invalidSlots.contains(nextSlot % (rows * 9))) {
            if (i > 1000) throw new RuntimeException("Couldn't find non-invalid slot");
            nextSlot++;
            i++;
        }
        data.put(nextSlot, slotEntry);
        nextSlot++;
        entries++;
    }
}
