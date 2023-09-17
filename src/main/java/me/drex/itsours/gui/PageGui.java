package me.drex.itsours.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.gui.util.GuiTextures;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static me.drex.message.api.LocalizedMessage.localized;

public abstract class PageGui<T> extends BaseGui {

    private static final int WIDTH = 9;
    private final List<CompletableFuture<GuiElementBuilder>> futures = new LinkedList<>();
    private int pageIndex = 0;
    private int filterIndex = 0;

    public PageGui(GuiContext context, ScreenHandlerType<? extends GenericContainerScreenHandler> type) {
        super(context, type);
        if (type == ScreenHandlerType.GENERIC_9X1) throw new IllegalArgumentException();
        if (getWidth() != WIDTH) throw new IllegalArgumentException();
    }

    @Override
    public void build() {
        futures.forEach(future -> future.cancel(true));
        futures.clear();
        int size = getSize();
        List<T> elements = filteredElements();
        for (int i = 0; i < WIDTH - 1; i++) {
            GuiElementBuilder builder = buildNavigationBar(i);
            int navigationSlot = size - WIDTH + i;
            if (builder != null) {
                this.setSlot(navigationSlot, builder);
            } else {
                this.clearSlot(navigationSlot);
            }
        }
        int pageElementSize = size - WIDTH;
        List<T> pageElements = elements.subList(pageIndex * pageElementSize, Math.min(elements.size(), (pageIndex + 1) * pageElementSize));
        for (int i = 0; i < pageElements.size(); i++) {
            T element = pageElements.get(i);
            this.setSlot(i, guiElement(element));
            CompletableFuture<GuiElementBuilder> future = guiElementFuture(element);
            if (future != null) {
                futures.add(future);
                final int finalI = i;
                future.thenAcceptAsync(guiElement -> this.setSlot(finalI, guiElement), Util.getMainWorkerExecutor());
            }
        }
        for (int i = pageElements.size(); i < pageElementSize; i++) {
            clearSlot(i);
        }
    }

    public GuiElementBuilder buildNavigationBar(int index) {
        return switch (index) {
            case 3:
                yield pageIndex != 0 ? new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(GuiTextures.GUI_PREVIOUS_PAGE)
                    .setName(localized("text.itsours.gui.previousPage"))
                    .setCallback(() -> updatePage(pageIndex - 1)) : null;
            case 4:
                yield new GuiElementBuilder(Items.BOOK)
                    .setName(localized("text.itsours.gui.currentPage", Map.of(
                        "page", Text.literal(String.valueOf(pageIndex + 1)),
                        "max_page", Text.literal(String.valueOf(maxPageIndex() + 1))
                    )));
            case 5:
                yield pageIndex != maxPageIndex() ? new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(GuiTextures.GUI_NEXT_PAGE)
                    .setName(localized("text.itsours.gui.nextPage"))
                    .setCallback(() -> updatePage(pageIndex + 1)) : null;
            case 6:
                yield !filters().isEmpty() ? new GuiElementBuilder(Items.PAPER)
                    .setName(localized("text.itsours.gui.filter"))
                    .setLore(
                        filters().stream().map(filter -> (Text) (filters().indexOf(filter) == filterIndex ? filter.text().copy().formatted(Formatting.BOLD) : filter.text())).toList()
                    )
                    .setCallback(clickType -> {
                        int diff;
                        if (clickType.isLeft) {
                            diff = 1;
                        } else if (clickType.isRight) {
                            diff = -1;
                        } else {
                            fail();
                            return;
                        }
                        filterIndex = Math.floorMod(filterIndex + diff, filters().size());
                        updatePage(pageIndex);
                    })
                    : null;
            default:
                yield null;
        };
    }

    public void updatePage(int pageIndex) {
        this.pageIndex = MathHelper.clamp(pageIndex, 0, maxPageIndex());
        click();
        rebuild();
    }

    protected List<Filter<T>> filters() {
        return Collections.emptyList();
    }

    private int maxPageIndex() {
        List<T> elements = filteredElements();
        if (elements.isEmpty()) return 0;
        return MathHelper.ceil(elements.size() / (double) (getSize() - WIDTH)) - 1;
    }

    public abstract Collection<T> elements();

    private List<T> filteredElements() {
        List<Filter<T>> filters = filters();
        Predicate<T> predicate;
        if (filters.isEmpty()) {
            predicate = (t) -> true;
        } else {
            predicate = filters.get(filterIndex).predicate();
        }
        return elements().stream().filter(predicate).toList();
    }

    protected abstract GuiElementBuilder guiElement(T element);

    @Nullable
    public CompletableFuture<GuiElementBuilder> guiElementFuture(T element) {
        return null;
    }

    public record Filter<T>(MutableText text, Predicate<T> predicate) {

    }

}
