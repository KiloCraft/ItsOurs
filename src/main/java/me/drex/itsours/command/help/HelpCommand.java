package me.drex.itsours.command.help;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.command.Command;
import me.drex.itsours.util.TextPage;
import net.minecraft.text.Text;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HelpCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, Integer> page = RequiredArgumentBuilder.argument("page", IntegerArgumentType.integer());
        page.executes(ctx -> sendHelp(ctx.getSource(), HelpCategory.getByID(StringArgumentType.getString(ctx, "category")), IntegerArgumentType.getInteger(ctx, "page") - 1));
        RequiredArgumentBuilder<ServerCommandSource, String> category = categoryArgument();
        category.executes(ctx -> sendHelp(ctx.getSource(), HelpCategory.getByID(StringArgumentType.getString(ctx, "category")), 0));
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("help");
        category.then(page);
        command.then(category);
        literal.then(command);
    }

    public static int sendHelp(ServerCommandSource src, HelpCategory category, int page) throws CommandSyntaxException {
        if (category == null) {
            src.sendError(Text.translatable("text.itsours.command.help.unknown_category"));
            return 0;
        }
        List<String> lines = new ArrayList<>();
        int from = 0;
        for (int i = 0; i < category.getPages().length; i++) {
            HelpCategory.HelpPage helpPage = category.getPages()[i];
            if (i < page) from += helpPage.getLines().length;
            lines.addAll(Arrays.asList(helpPage.getLines()));
        }
        int to = from + category.getPages()[page].getLines().length;
        TextPage textPage = new TextPage("" + category.getPages()[page].getTitle(), lines, category.getCommand());
        textPage.setNumberFormatting("", Formatting.YELLOW);
        textPage.sendEntries(src.getPlayer(), page, category.getPages().length - 1, from, to);
        return 1;
    }
}
