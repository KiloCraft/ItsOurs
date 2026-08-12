package me.drex.itsours.util;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.node.DynamicTextNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TagLikeParser;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.server.translations.api.Localization;
import xyz.nucleoid.server.translations.api.LocalizationTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface LocalizedMessage {

    ParserContext.Key<Function<String, @Nullable Text>> STATIC_PLACEHOLDERS = DynamicTextNode.key("itsours:static_placeholders");
    NodeParser PARSER = NodeParser.builder()
        .quickText()
        .simplifiedTextFormat()
        .globalPlaceholders()
        .placeholders(TagLikeParser.PLACEHOLDER_USER, STATIC_PLACEHOLDERS)
        .build();
    Style DEFAULT_LORE_STYLE = Style.EMPTY
        .withColor(Formatting.WHITE)
        .withItalic(false);

    static MutableText localized(String key) {
        return localized(key, Map.of(), LocalizationTarget.ofSystem(), null);
    }

    static MutableText localized(String key, Map<String, Text> placeholders) {
        return localized(key, placeholders, LocalizationTarget.ofSystem(), null);
    }

    static MutableText localized(String key, PlaceholderContext context) {
        return localized(key, Map.of(), context);
    }

    static MutableText localized(String key, Map<String, Text> placeholders, PlaceholderContext context) {
        LocalizationTarget target = context.player() != null ? LocalizationTarget.of(context.player()) : LocalizationTarget.ofSystem();
        return localized(key, placeholders, target, context);
    }

    static MutableText localized(String key, ServerCommandSource source) {
        return localized(key, Map.of(), source);
    }

    static MutableText localized(String key, Map<String, Text> placeholders, ServerCommandSource source) {
        return localized(key, placeholders, PlaceholderContext.of(source));
    }

    static MutableText localized(String key, ServerPlayerEntity player) {
        return localized(key, Map.of(), player);
    }

    static MutableText localized(String key, PlayerEntity player) {
        return localized(key, Map.of(), player);
    }

    static MutableText localized(String key, Map<String, Text> placeholders, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            return localized(key, placeholders, serverPlayer);
        }
        return localized(key, placeholders);
    }

    static MutableText localized(String key, Map<String, Text> placeholders, ServerPlayerEntity player) {
        return localized(key, placeholders, LocalizationTarget.of(player), PlaceholderContext.of(player));
    }

    static List<Text> splitLines(Text text) {
        List<Text> result = new ArrayList<>();
        MutableText[] currentLine = {Text.empty()};
        splitLines(currentLine, DEFAULT_LORE_STYLE, text, result);
        if (!currentLine[0].getSiblings().isEmpty()) {
            result.add(currentLine[0]);
        }
        return result;
    }

    static List<Text> splitLines(Iterable<? extends Text> texts) {
        List<Text> result = new ArrayList<>();
        for (Text text : texts) {
            result.addAll(splitLines(text));
        }
        return result;
    }

    private static MutableText localized(String key, Map<String, Text> placeholders, LocalizationTarget target, @Nullable PlaceholderContext context) {
        String message = Localization.raw(key, target);
        if (message == null) {
            message = key;
        }
        ParserContext parserContext = context != null ? context.asParserContext() : ParserContext.of();
        parserContext.with(STATIC_PLACEHOLDERS, placeholderGetter(placeholders));
        return (MutableText) PARSER.parseText(TextNode.of(message), parserContext);
    }

    private static Function<String, @Nullable Text> placeholderGetter(Map<String, Text> placeholders) {
        return key -> placeholders.getOrDefault(key, Text.literal("${" + key + "}"));
    }

    private static void splitLines(MutableText[] currentLine, Style parentStyle, Text text, List<Text> result) {
        TextContent content = text.getContent();
        Style style = text.getStyle().withParent(parentStyle);
        if (content instanceof PlainTextContent.Literal literal) {
            String[] lines = literal.string().split("\n", -1);
            for (int i = 0; i < lines.length; i++) {
                if (i > 0) {
                    result.add(currentLine[0]);
                    currentLine[0] = Text.empty();
                }
                if (!lines[i].isEmpty()) {
                    currentLine[0].append(Text.literal(lines[i]).setStyle(style));
                }
            }
        } else {
            currentLine[0].append(MutableText.of(content).setStyle(style));
        }
        for (Text sibling : text.getSiblings()) {
            splitLines(currentLine, style, sibling, result);
        }
    }

}
