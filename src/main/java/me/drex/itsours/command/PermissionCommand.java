package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionList;
import me.drex.itsours.claim.permission.util.context.ContextEntry;
import me.drex.itsours.claim.permission.util.context.PermissionContext;
import me.drex.itsours.claim.permission.util.node.util.Node;
import me.drex.itsours.util.Colors;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

public class PermissionCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = permissionClaimArgument("modify.permission");
        {
            RequiredArgumentBuilder<ServerCommandSource, String> permission = permissionArgument();
            permission.executes(PermissionCommand::checkPlayer);
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = players();
            player.executes(PermissionCommand::listPermission);
            LiteralArgumentBuilder<ServerCommandSource> check = LiteralArgumentBuilder.literal("check");
            player.then(permission);
            check.then(player);
            claim.then(check);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, String> value = permissionValueArgument();
            value.executes(PermissionCommand::setPermission);
            RequiredArgumentBuilder<ServerCommandSource, String> permission = permissionArgument();
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = players();
            LiteralArgumentBuilder<ServerCommandSource> set = LiteralArgumentBuilder.literal("set");
            permission.then(value);
            player.then(permission);
            set.then(player);
            claim.then(set);
        }
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("permission");
        command.executes(ctx -> listPermissions(ctx.getSource()));
        command.then(claim);
        literal.then(command);
    }

    public static int checkPlayer(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        AbstractClaim claim = getClaim(ctx);
        validatePermission(claim, source, "modify.permission");
        Permission permission = getPermission(ctx);

        getGameProfile(ctx).thenAccept(optional -> {
            optional.ifPresent(gameProfile -> {
                PermissionContext context = claim.getContext(gameProfile.getId(), permission);
                MutableText hover = Text.empty();
                MutableText builder = Text.empty();
                for (ContextEntry entry : context.getEntries()) {
                    builder.append(Text.translatable("text.itsours.command.permission.check.hover",
                            Text.literal(claim.getName()).formatted(Formatting.LIGHT_PURPLE),
                            Text.literal(String.valueOf(claim.getDepth())).formatted(Formatting.DARK_PURPLE),
                            entry.getPriority().getName(),
                            Text.literal(entry.getPermission().asString()).formatted(Formatting.GRAY),
                            entry.getValue().format()
                    ));
                    hover.append("\n");
                }
                ctx.getSource().sendFeedback(Text.translatable("text.itsours.command.permission.check",
                        permission.asString(),
                        claim.getFullName(),
                        context.getValue().format(),
                        gameProfile
                ).styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))), false);
            });
        });
        return 1;
    }

    public static void setPermission(ServerCommandSource source, AbstractClaim claim, GameProfile profile, Permission permission, Permission.Value value) throws CommandSyntaxException {
        validatePermission(claim, source, "modify.permission");
        claim.getPermissionManager().setPlayerPermission(profile.getId(), permission, value);
        source.sendFeedback(Text.translatable("text.itsours.command.permission.set",
                permission.asString(),
                claim.getFullName(),
                profile.getName(),
                value.format()
        ), false);
    }

    public static int setPermission(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        AbstractClaim claim = getClaim(ctx);
        Permission permission = getPermission(ctx);
        Permission.Value value = getPermissionValue(ctx);
        getGameProfile(ctx).thenAccept(optional -> {
            optional.ifPresent(gameProfile -> {
                try {
                    setPermission(source, claim, gameProfile, permission, value);
                } catch (CommandSyntaxException e) {
                    source.sendError(Texts.toText(e.getRawMessage()));
                }
            });
        });
        return 1;
    }

    public static int listPermission(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        AbstractClaim claim = getClaim(ctx);
        validatePermission(claim, ctx.getSource(), "modify.permission");
        getGameProfile(ctx).thenAccept(optional -> {
            optional.ifPresent(gameProfile -> {
                ctx.getSource().sendFeedback(Text.translatable("text.itsours.command.permission.list", gameProfile).formatted(Colors.TITLE_COLOR), false);
                ctx.getSource().sendFeedback(Text.translatable("text.itsours.command.permission.list.claim", claim.getFullName()).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/claim info %s", claim.getFullName())))), false);
                //ctx.getSource().sendFeedback(Text.translatable("text.itsours.command.permission.list.roles", claim.getFullName()).onClick(ClickEvent.Action.RUN_COMMAND, String.format("/claim info %s", claim.getFullName())), false);
                ctx.getSource().sendFeedback(Text.translatable("text.itsours.command.permission.list.permissions", claim.getPermissionManager().getPlayerPermission(gameProfile.getId()).toText()), false);
            });
        });
        return 1;
    }

    public static int listPermissions(ServerCommandSource source) {
        source.sendFeedback(Text.translatable("text.itsours.command.permission").formatted(Colors.TITLE_COLOR), false);
        for (Node node : PermissionList.INSTANCE.permission.getNodes()) {
            source.sendFeedback(Text.translatable("text.itsours.command.permission.format", Text.literal(node.getId()).formatted(Colors.PRIMARY_COLOR), node.getInformation()), false);
        }
        return 1;
    }

}
