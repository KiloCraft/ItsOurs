package me.drex.itsours.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.FlagsManager;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.claim.flags.node.RootNode;
import me.drex.itsours.claim.flags.util.InvalidFlagException;
import me.drex.itsours.claim.flags.util.Value;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.*;

import static me.drex.message.api.LocalizedMessage.localized;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.text.Text.literal;
import static net.minecraft.util.Identifier.NAMESPACE_SEPARATOR;

public class FlagArgument {

    public static final DynamicCommandExceptionType UNKNOWN_FLAG = new DynamicCommandExceptionType(input -> localized("text.itsours.argument.flags.unknown", Map.of("input", literal(input.toString()))));
    public static final DynamicCommandExceptionType UNKNOWN_VALUE = new DynamicCommandExceptionType(input -> localized("text.itsours.argument.value.invalid", Map.of("input", literal(input.toString()))));
    public static final CommandSyntaxException FORBIDDEN = new SimpleCommandExceptionType(localized("text.itsours.argument.flags.forbidden")).create();
    public static final SuggestionProvider<ServerCommandSource> PLAYER_FLAG_PROVIDER = (context, builder) -> {
        return CommandSource.suggestMatching(generateCandidates(FlagsManager.PLAYER, builder.getRemaining()), builder);
    };
    public static final SuggestionProvider<ServerCommandSource> FLAG_PROVIDER = (context, builder) -> {
        return CommandSource.suggestMatching(generateCandidates(FlagsManager.GLOBAL, builder.getRemaining()), builder);
    };
    public static final SuggestionProvider<ServerCommandSource> VALUE_PROVIDER = (source, builder) -> CommandSource.suggestMatching(Arrays.stream(Value.values()).map(value -> value.literal), builder);

    public static RequiredArgumentBuilder<ServerCommandSource, String> playerFlag() {
        return playerFlag("player_flag");
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> flag() {
        return flag("flag");
    }

    public static Flag getPlayerFlag(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return getPlayerFlag(ctx, "player_flag");
    }

    public static Flag getPlayerFlag(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        return getPlayerFlag(ctx, name, FlagsManager.PLAYER);
    }

    public static Flag getFlag(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return getFlag(ctx, "flag");
    }

    public static Flag getFlag(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        return getPlayerFlag(ctx, name, FlagsManager.GLOBAL);
    }

    private static Flag getPlayerFlag(CommandContext<ServerCommandSource> ctx, String name, RootNode rootNode) throws CommandSyntaxException {
        String string = StringArgumentType.getString(ctx, name).replace('+', NAMESPACE_SEPARATOR);
        try {
            return Flag.valueOf(rootNode, string);
        } catch (InvalidFlagException e) {
            throw UNKNOWN_FLAG.create(string);
        }
    }

    public static Value getValue(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return getValue(ctx, "value");
    }

    public static Value getValue(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        String string = StringArgumentType.getString(ctx, name);
        Optional<Value> optional = Arrays.stream(Value.values()).filter(value -> value.literal.equals(string)).findFirst();
        return optional.orElseThrow(() -> UNKNOWN_VALUE.create(string));
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> playerFlag(String name) {
        return argument(name, StringArgumentType.string()).suggests(PLAYER_FLAG_PROVIDER);
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> flag(String name) {
        return argument(name, StringArgumentType.string()).suggests(FLAG_PROVIDER);
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> value() {
        return value("value");
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> value(String name) {
        return argument(name, StringArgumentType.word()).suggests(VALUE_PROVIDER);
    }

    private static List<String> generateCandidates(final RootNode rootNode, final String input) {
        final List<String> candidates = new ArrayList<>();
        for (ChildNode childNode : rootNode.getNodes()) {
            addSubNodes(childNode, childNode.getId(), input, candidates);
        }
        return candidates;
    }

    private static void addSubNodes(final Node node, String currentId, final String input, final List<String> result) {
        // We need to replace ':' with '+', because it's the only character
        // that is not valid in identifiers, but can be parsed by StringArgumentType.word()
        currentId = currentId.replace(NAMESPACE_SEPARATOR, '+');
        result.add(currentId);
        for (ChildNode childNode : node.getNodes()) {
            if (input.startsWith(currentId)) {
                addSubNodes(childNode, currentId + "." + childNode.getId(), input, result);
            }
        }
    }

}
