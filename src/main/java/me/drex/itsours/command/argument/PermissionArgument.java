package me.drex.itsours.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionImpl;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.claim.permission.node.RootNode;
import me.drex.itsours.claim.permission.util.InvalidPermissionException;
import me.drex.itsours.claim.permission.util.Value;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.minecraft.server.command.CommandManager.argument;

public class PermissionArgument {

    public static final DynamicCommandExceptionType UNKNOWN_PERMISSION = new DynamicCommandExceptionType(name -> Text.translatable("text.itsours.argument.permission.unknown", name));
    public static final DynamicCommandExceptionType UNKNOWN_PERMISSION_VALUE = new DynamicCommandExceptionType(name -> Text.translatable("text.itsours.argument.permission.value.unknown", name));
    public static final CommandSyntaxException FORBIDDEN = new SimpleCommandExceptionType(Text.translatable("text.itsours.argument.permission.forbidden")).create();


    public static RequiredArgumentBuilder<ServerCommandSource, String> permission() {
        return permission("permission");
    }

    public static Permission getPermission(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return getPermission(ctx, "permission");
    }

    public static Permission getPermission(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        String string = StringArgumentType.getString(ctx, name);
        try {
            return PermissionImpl.fromId(string);
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
        return argument(name, StringArgumentType.word()).suggests(PERMISSION_PROVIDER);
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> value() {
        return value("value");
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> value(String name) {
        return argument(name, StringArgumentType.word()).suggests(VALUE_PROVIDER);
    }

    public static final SuggestionProvider<ServerCommandSource> PERMISSION_PROVIDER = (source, builder) -> {
        final List<String> result = new ArrayList<>();
        addNodes(PermissionManager.COMBINED, builder.getRemaining(), result);
        return CommandSource.suggestMatching(result, builder);
    };

    protected static void addNodes(final RootNode node, final String input, final List<String> result) {
        for (Node subNode : node.getNodes()) {
            addSubNodes(subNode, subNode.getId(), input, result);
        }
    }

    private static void addSubNodes(final Node node, final String currentId, final String input, final List<String> result) {
        result.add(currentId);
        for (Node subNode : node.getNodes()) {
            if (input.startsWith(currentId)) {
                addSubNodes(subNode, currentId + "." + subNode.getId(), input, result);
            }
        }
    }

    public static final SuggestionProvider<ServerCommandSource> VALUE_PROVIDER = (source, builder) -> CommandSource.suggestMatching(Arrays.stream(new Value[]{Value.ALLOW, Value.DENY}).map(value -> value.literal), builder);

}
