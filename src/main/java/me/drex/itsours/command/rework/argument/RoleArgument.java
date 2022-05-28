package me.drex.itsours.command.rework.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.drex.itsours.claim.permission.rework.roles.RoleManagerRework;
import me.drex.itsours.claim.permission.rework.roles.RoleRework;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;

public class RoleArgument {

    public static final DynamicCommandExceptionType UNKNOWN_ROLE = new DynamicCommandExceptionType(name -> Text.translatable("text.itsours.argument.role.unknown", name));
    private static final String DEFAULT_NAME = "role";

    public static RequiredArgumentBuilder<ServerCommandSource, String> roles() {
        return argument(DEFAULT_NAME, StringArgumentType.word()).suggests(ROLES_PROVIDER);
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> roles(String name) {
        return argument(name, StringArgumentType.word()).suggests(ROLES_PROVIDER);
    }

    public static RoleRework getRole(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return getRole(ctx, DEFAULT_NAME);
    }

    public static RoleRework getRole(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        String string = StringArgumentType.getString(ctx, name);
        RoleRework role = RoleManagerRework.INSTANCE.getRole(string);
        if (role != null) return role;
        throw UNKNOWN_ROLE.create(string);
    }

    public static final SuggestionProvider<ServerCommandSource> ROLES_PROVIDER = (source, builder) -> {
        final List<String> result = RoleManagerRework.INSTANCE.getOrderedRoles().stream().map(RoleRework::getId).toList();
        return CommandSource.suggestMatching(result, builder);
    };

}
