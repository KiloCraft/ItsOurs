package me.drex.itsours.util;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.ServerPlaceholderContext;
import eu.pb4.placeholders.api.node.DynamicTextNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TagLikeParser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.server.translations.api.Localization;
import xyz.nucleoid.server.translations.api.LocalizationTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface LocalizedMessage {

    ParserContext.Key<Function<String, @Nullable Component>> STATIC_PLACEHOLDERS = DynamicTextNode.key("itsours:static_placeholders");
    NodeParser PARSER = NodeParser.builder()
        .quickText()
        .simplifiedTextFormat()
        .serverPlaceholders()
        .placeholders(TagLikeParser.PLACEHOLDER_USER, STATIC_PLACEHOLDERS)
        .build();
    Style DEFAULT_LORE_STYLE = Style.EMPTY
        .withColor(ChatFormatting.WHITE)
        .withItalic(false);

    static MutableComponent localized(String key) {
        return localized(key, Map.of(), LocalizationTarget.ofSystem(), null);
    }

    static MutableComponent localized(String key, Map<String, Component> placeholders) {
        return localized(key, placeholders, LocalizationTarget.ofSystem(), null);
    }

    static MutableComponent localized(String key, PlaceholderContext context) {
        return localized(key, Map.of(), context);
    }

    static MutableComponent localized(String key, Map<String, Component> placeholders, PlaceholderContext context) {
        LocalizationTarget target = context.player() instanceof ServerPlayer player ? LocalizationTarget.of(player) : LocalizationTarget.ofSystem();
        return localized(key, placeholders, target, context);
    }

    static MutableComponent localized(String key, CommandSourceStack source) {
        return localized(key, Map.of(), source);
    }

    static MutableComponent localized(String key, Map<String, Component> placeholders, CommandSourceStack source) {
        return localized(key, placeholders, ServerPlaceholderContext.of(source));
    }

    static MutableComponent localized(String key, ServerPlayer player) {
        return localized(key, Map.of(), player);
    }

    static MutableComponent localized(String key, Player player) {
        return localized(key, Map.of(), player);
    }

    static MutableComponent localized(String key, Map<String, Component> placeholders, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return localized(key, placeholders, serverPlayer);
        }
        return localized(key, placeholders);
    }

    static MutableComponent localized(String key, Map<String, Component> placeholders, ServerPlayer player) {
        return localized(key, placeholders, LocalizationTarget.of(player), ServerPlaceholderContext.of(player));
    }

    static List<Component> splitLines(Component text) {
        List<Component> result = new ArrayList<>();
        MutableComponent[] currentLine = {Component.empty()};
        splitLines(currentLine, DEFAULT_LORE_STYLE, text, result);
        if (!currentLine[0].getSiblings().isEmpty()) {
            result.add(currentLine[0]);
        }
        return result;
    }

    static List<Component> splitLines(Iterable<? extends Component> texts) {
        List<Component> result = new ArrayList<>();
        for (Component text : texts) {
            result.addAll(splitLines(text));
        }
        return result;
    }

    private static MutableComponent localized(String key, Map<String, Component> placeholders, LocalizationTarget target, @Nullable PlaceholderContext context) {
        String message = Localization.raw(key, target);
        if (message == null) {
            message = key;
        }
        ParserContext parserContext = context != null ? context.asParserContext() : ParserContext.of();
        parserContext.with(STATIC_PLACEHOLDERS, placeholderGetter(placeholders));
        return (MutableComponent) PARSER.parseComponent(TextNode.of(message), parserContext);
    }

    static void sendMessage(Player player, Component message, boolean overlay) {
        if (overlay) {
            player.sendOverlayMessage(message);
        } else {
            player.sendSystemMessage(message);
        }
    }

    private static Function<String, @Nullable Component> placeholderGetter(Map<String, Component> placeholders) {
        return key -> placeholders.getOrDefault(key, Component.literal("${" + key + "}"));
    }

    private static void splitLines(MutableComponent[] currentLine, Style parentStyle, Component text, List<Component> result) {
        ComponentContents content = text.getContents();
        Style style = text.getStyle().applyTo(parentStyle);
        if (content instanceof PlainTextContents.LiteralContents literal) {
            String[] lines = literal.text().split("\n", -1);
            for (int i = 0; i < lines.length; i++) {
                if (i > 0) {
                    result.add(currentLine[0]);
                    currentLine[0] = Component.empty();
                }
                if (!lines[i].isEmpty()) {
                    currentLine[0].append(Component.literal(lines[i]).setStyle(style));
                }
            }
        } else {
            currentLine[0].append(MutableComponent.create(content).setStyle(style));
        }
        for (Component sibling : text.getSiblings()) {
            splitLines(currentLine, style, sibling, result);
        }
    }

}
