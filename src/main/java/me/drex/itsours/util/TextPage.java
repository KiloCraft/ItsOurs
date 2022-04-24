package me.drex.itsours.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class TextPage {

    public final Text title;
    private final String command;
    public List<Text> entries;
    private String number = "%s. ";
    private Formatting numberFormat = Formatting.YELLOW;

    public TextPage(MutableText title, List<Text> entries, String command) {
        this.title = title;
        this.entries = entries == null ? new ArrayList<>() : entries;
        this.command = command;
    }

    public TextPage(String title, List<String> entries, String command) {
        this.title = Text.literal(title);
        ArrayList<Text> list = new ArrayList<>();
        if (entries != null) {
            for (String entry : entries) {
                list.add(Text.literal(entry));
            }
        }

        this.entries = list;
        this.command = command;
    }

    public void addEntry(Text text) {
        entries.add(text);
    }

    public void setNumberFormatting(String number, Formatting numberFormat) {
        this.number = number;
        this.numberFormat = numberFormat;
    }

    /**
     * @param player         who will receive the message
     * @param page           to be sent (first = 0), -1 will use the last page
     * @param entriesPerPage per page
     */
    public void sendPage(ServerPlayerEntity player, int page, int entriesPerPage) {
        int entries = this.entries.size();
        int maxPage = (entries - 1) / entriesPerPage;
        page = page == -1 ? maxPage : page;
        //index
        int from = page * entriesPerPage;
        int to = Math.min(((page + 1) * entriesPerPage), entries);
        sendEntries(player, page, maxPage, from, to);
    }

    public void sendEntries(ServerPlayerEntity player, int page, int maxPage, int from, int to) {
        MutableText message = Text.empty().append(title);
        int currentIndex = from + 1;
        for (Text text : this.entries.subList(from, to)) {
            message.append(Text.literal("\n" + String.format(this.number, currentIndex)).formatted(numberFormat)).append(text);
            currentIndex++;
        }
        message.append(Text.literal("\n<- ").formatted(Formatting.WHITE).styled(style -> style.withBold(true)))
                .append(Text.literal("Prev ").formatted(page > 0 ? Formatting.GOLD : Formatting.GRAY)
                        .styled(style -> page > 0 ? style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(this.command, page))) : style))
                .append(Text.literal(String.valueOf(page + 1)).formatted(Formatting.GREEN))
                .append(Text.literal(" / ").formatted(Formatting.GRAY))
                .append(Text.literal(String.valueOf(maxPage + 1)).formatted(Formatting.GREEN))
                .append(Text.literal(" Next").formatted(page == maxPage ? Formatting.GRAY : Formatting.GOLD)
                        .styled(style -> page == maxPage ? style : style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(this.command, page + 2)))))
                .append(Text.literal(" ->").formatted(Formatting.WHITE).styled(style -> style.withBold(true)));
        player.sendMessage(message, false);
    }
}