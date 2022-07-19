package me.drex.itsours.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class PagedGui<T> extends ClaimGui {
    public static final int PAGE_SIZE = 9 * 5;
    protected int page = 0;
    private int sortModeIndex = 0;

    public PagedGui(ServerPlayerEntity player, @Nullable SimpleGui previousGui) {
        super(ScreenHandlerType.GENERIC_9X6, player, previousGui);
    }

    protected void nextPage() {
        this.page = Math.min(this.getPageAmount() - 1, this.page + 1);
        this.updateDisplay();
    }

    protected boolean canNextPage() {
        return this.getPageAmount() > this.page + 1;
    }

    protected void previousPage() {
        this.page = Math.max(0, this.page - 1);
        this.updateDisplay();
    }

    protected boolean canPreviousPage() {
        return this.page - 1 >= 0;
    }

    public int getPage() {
        return this.page;
    }

    protected void updateDisplay() {
        var offset = this.page * PAGE_SIZE;

        List<T> elements = getElements();
        List<SortMode<T>> sortModes = getSortModes();
        if (!sortModes.isEmpty()) {
            SortMode<T> sortMode = sortModes.get(sortModeIndex);
            elements.sort(sortMode.comparator);
        }

        for (int i = 0; i < PAGE_SIZE; i++) {
            int id = offset + i;
            GuiElement element;
            if (elements.size() > id) {
                element = asDisplayElement(elements.get(id));
            } else {
                element = EMPTY;
            }
            this.setSlot(i, element);
        }

        for (int i = 0; i < 9; i++) {
            this.setSlot(i + PAGE_SIZE, this.getNavElement(i));
        }
    }

    protected abstract List<T> getElements();

    public List<SortMode<T>> getSortModes() {
        return Collections.emptyList();
    }

    /**
     * Executes when the screen is opened
     */
    @Override
    public void onOpen() {
        super.onOpen();
        updateDisplay();

    }

    protected abstract GuiElement asDisplayElement(T element);

    protected int getPageAmount() {
        return MathHelper.ceil(this.getElements().size() / (double) PAGE_SIZE);
    }

    protected GuiElement getNavElement(int id) {
        return switch (id) {
            case 3 -> previousPageElement();
            case 4 -> pageInfo();
            case 5 -> nextPageElement();
            case 8 -> new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.translatable("gui.back").formatted(Formatting.RED))
                    .hideFlags()
                    .setCallback((x, y, z) -> {
                        playClickSound(this.player);
                        this.close();
                    }).build();
            case 7 -> sortMode();
            default -> FILLER;
        };
    }


    public GuiElement previousPageElement() {
        if (canPreviousPage()) {
            return new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setName(Text.translatable("spectatorMenu.previous_page").formatted(Formatting.WHITE))
                    .hideFlags()
                    .setSkullOwner(GuiTextures.GUI_PREVIOUS_PAGE)
                    .setCallback((x, y, z) -> {
                        playClickSound(player);
                        previousPage();
                    }).build();
        } else {
            return FILLER;
        }
    }

    public GuiElement nextPageElement() {
        if (canNextPage()) {
            return new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setName(Text.translatable("spectatorMenu.next_page").formatted(Formatting.WHITE))
                    .hideFlags()
                    .setSkullOwner(GuiTextures.GUI_NEXT_PAGE)
                    .setCallback((x, y, z) -> {
                        playClickSound(player);
                        nextPage();
                    }).build();
        } else {
            return FILLER;
        }
    }

    public GuiElement pageInfo() {
        if (getPageAmount() != 0) {
            return new GuiElementBuilder(Items.WRITABLE_BOOK)
                    .setName(Text.translatable("text.itsours.gui.page", getPage() + 1, getPageAmount()).formatted(Formatting.WHITE))
                    .hideFlags().build();
        } else {
            return FILLER;
        }
    }

    public GuiElement sortMode() {
        if (getSortModes().size() > 1) {
            List<MutableText> lore = new ArrayList<>(getSortModes().stream().map(SortMode::text).toList());
            lore.set(sortModeIndex, lore.get(sortModeIndex).formatted(Formatting.AQUA));
            return new GuiElementBuilder(Items.COMPASS)
                    .setName(Text.translatable("text.itsours.gui.sortMode").formatted(Formatting.WHITE))
                    .hideFlags()
                    .setLore(
                            Collections.unmodifiableList(lore)
                    )
                    .setCallback((x, y, z) -> {
                        sortModeIndex = (sortModeIndex + 1) % getSortModes().size();
                        playClickSound(player);
                        updateDisplay();
                    }).build();
        } else {
            return FILLER;
        }
    }

    public record SortMode<T>(MutableText text, Comparator<T> comparator) {

    }


}
