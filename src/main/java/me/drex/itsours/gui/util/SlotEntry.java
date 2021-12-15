package me.drex.itsours.gui.util;

import me.drex.itsours.gui.screen.SimpleScreen;
import me.drex.itsours.gui.util.context.NoContext;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class SlotEntry<K extends NoContext> {

    private ItemStack item = new ItemStack(Items.AIR);
    private Consumer<K> consumer = (context, leftClick, shiftClick) -> {
    };


    public SlotEntry() {
    }

    public SlotEntry(Item item) {
        this.item = new ItemStack(item);
    }

    public SlotEntry(ItemStack item) {
        this.item = item;
    }

    public SlotEntry(ItemStack item, Consumer<K> consumer) {
        this.item = item;
        this.consumer = consumer;
    }

    public SlotEntry(Item item, Consumer<K> consumer) {
        this.item = new ItemStack(item);
        this.consumer = consumer;
    }

    public SlotEntry(Item item, String name, Consumer<K> consumer) {
        this.item = new ItemStack(item);
        ScreenHelper.setCustomName(this.item, name);
        this.consumer = consumer;
    }

    public SlotEntry(Item item, String name) {
        this.item = new ItemStack(item);
        ScreenHelper.setCustomName(this.item, name);
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public void onClick(Consumer<K> consumer) {
        this.consumer = consumer;
    }

    public void handleClick(SimpleScreen<K> screen, K context, boolean leftClick, boolean shiftClick) {
        consumer.accept(context, leftClick, shiftClick);
    }

    public void render(SimpleScreen<K> screen, Inventory inventory, int slotIndex) {
        inventory.setStack(slotIndex, item);
    }

}