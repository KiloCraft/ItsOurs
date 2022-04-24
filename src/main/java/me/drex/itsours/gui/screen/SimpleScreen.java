package me.drex.itsours.gui.screen;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import me.drex.itsours.gui.GUIScreenHandler;
import me.drex.itsours.gui.util.ScreenSync;
import me.drex.itsours.gui.util.SlotEntry;
import me.drex.itsours.gui.util.context.NoContext;
import me.drex.itsours.mixin.PlayerEntityInvoker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;


public class SimpleScreen<K extends NoContext> extends GUIScreenHandler {

    protected final Int2ObjectMap<SlotEntry<K>> data = new Int2ObjectArrayMap<>();
    protected final SlotEntry<K> fill = new SlotEntry<>(Items.GRAY_STAINED_GLASS_PANE);
    protected K context;
    private final ScreenHandler instance = this;

    public SimpleScreen(ServerPlayerEntity player, int rows, K context) {
        super(0, rows, player);
        this.context = context;
    }

    @Override
    protected void fillInventory(PlayerEntity player, Inventory inv) {
        for (int slotIndex = 0; slotIndex < inv.size(); slotIndex++) {
            SlotEntry<K> slotEntry = getEntry(slotIndex);
            if (slotEntry == null) slotEntry = fill;
            slotEntry.render(this, inventory, slotIndex);
        }
    }

    public void draw() {
        fillInventory(player, inventory);
    }

    public void close() {
        ((PlayerEntityInvoker)player).closeHandledScreen();
    }

    @Override
    protected void handleSlotClick(ServerPlayerEntity player, int index, Slot slot, boolean leftClick, boolean shiftClick) {
        SlotEntry<K> slotEntry = getEntry(index);
        if (slotEntry != null) slotEntry.handleClick(this, context, leftClick, shiftClick);
    }

    public SlotEntry<K> getEntry(int index) {
        return data.get(index);
    }

    public void addSlot(SlotEntry<K> slotEntry, int slotIndex) {
        data.put(slotIndex, slotEntry);
    }

    public void render() {
        draw();
        NamedScreenHandlerFactory factory = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                ((ScreenSync) instance).setSyncId(syncId);
                return instance;
            }

            @Override
            public Text getDisplayName() {
                return Text.literal(getTitle());
            }
        };
        player.openHandledScreen(factory);
    }

    protected String getTitle() {
        return "Inventory";
    }


}
