package me.drex.itsours.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.ChildNode;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.claim.permission.node.RootNode;
import me.drex.itsours.claim.permission.util.InvalidPermissionException;
import me.drex.itsours.claim.permission.util.Value;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.*;

import static me.drex.message.api.LocalizedMessage.localized;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.text.Text.literal;
import static net.minecraft.util.Identifier.NAMESPACE_SEPARATOR;

public class PermissionArgument {

    public static final DynamicCommandExceptionType UNKNOWN_PERMISSION = new DynamicCommandExceptionType(input -> localized("text.itsours.argument.permission.unknown", Map.of("input", literal(input.toString()))));
    public static final DynamicCommandExceptionType UNKNOWN_PERMISSION_VALUE = new DynamicCommandExceptionType(input -> localized("text.itsours.argument.permission.value.invalid", Map.of("input", literal(input.toString()))));
    public static final CommandSyntaxException FORBIDDEN = new SimpleCommandExceptionType(localized("text.itsours.argument.permission.forbidden")).create();
    public static final SuggestionProvider<ServerCommandSource> PERMISSION_PROVIDER = (context, builder) -> {
        return CommandSource.suggestMatching(generateCandidates(PermissionManager.PERMISSION, builder.getRemaining()), builder);
    };
    public static final SuggestionProvider<ServerCommandSource> SETTING_PROVIDER = (context, builder) -> {
        return CommandSource.suggestMatching(generateCandidates(PermissionManager.COMBINED, builder.getRemaining()), builder);
    };
    public static final SuggestionProvider<ServerCommandSource> VALUE_PROVIDER = (source, builder) -> CommandSource.suggestMatching(Arrays.stream(new Value[]{Value.ALLOW, Value.DENY}).map(value -> value.literal), builder);

    public static RequiredArgumentBuilder<ServerCommandSource, String> permission() {
        return permission("permission");
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> setting() {
        return setting("setting");
    }

    public static Permission getPermission(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return getPermission(ctx, "permission");
    }

    public static Permission getPermission(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        return getPermission(ctx, name, PermissionManager.PERMISSION);
    }

    public static Permission getSetting(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return getSetting(ctx, "setting");
    }

    public static Permission getSetting(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        return getPermission(ctx, name, PermissionManager.COMBINED);
    }

    private static Permission getPermission(CommandContext<ServerCommandSource> ctx, String name, RootNode rootNode) throws CommandSyntaxException {
        String string = StringArgumentType.getString(ctx, name).replace('+', NAMESPACE_SEPARATOR);
        try {
            return Permission.valueOf(rootNode, string);
        } catch (InvalidPermissionException e) {
            throw UNKNOWN_PERMISSION.create(string);
        }
    }

    public static Value getValue(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return getValue(ctx, "value");
    }

    public static Value getValue(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        String string = StringArgumentType.getString(ctx, name);
        Optional<Value> optional = Arrays.stream(Value.values()).filter(value -> value.literal.equals(string)).findFirst();
        return optional.orElseThrow(() -> UNKNOWN_PERMISSION_VALUE.create(string));
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> permission(String name) {
        return argument(name, StringArgumentType.string()).suggests(PERMISSION_PROVIDER);
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> setting(String name) {
        return argument(name, StringArgumentType.string()).suggests(SETTING_PROVIDER);
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
