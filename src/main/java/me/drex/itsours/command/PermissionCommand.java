package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionList;
import me.drex.itsours.claim.permission.util.context.ContextEntry;
import me.drex.itsours.claim.permission.util.context.PermissionContext;
import me.drex.itsours.claim.permission.util.node.util.Node;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;


public class PermissionCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = permissionClaimArgument("modify.permission");
        {
            RequiredArgumentBuilder<ServerCommandSource, String> permission = permissionArgument();
            permission.executes(ctx -> checkPlayer(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), getPermission(ctx)));
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            player.executes(ctx -> listPermission(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player")));
            LiteralArgumentBuilder<ServerCommandSource> check = LiteralArgumentBuilder.literal("check");
            player.then(permission);
            check.then(player);
            claim.then(check);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, String> value = permissionValueArgument();
            value.executes(ctx -> setPermission(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), getPermission(ctx), getPermissionValue(ctx)));
            RequiredArgumentBuilder<ServerCommandSource, String> permission = permissionArgument();
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
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

    public static int checkPlayer(ServerCommandSource source, AbstractClaim claim, GameProfile target, Permission permission) throws CommandSyntaxException {
        validatePermission(claim, source.getPlayer().getUuid(), "modify.permission");
        PermissionContext context = claim.getContext(target.getId(), permission);
        TextComponent.Builder hover = Component.text();
        for (ContextEntry entry : context.getEntries()) {
            hover.append(Component.text(entry.getClaim().getName()).color(Color.PURPLE))
                    .append(Component.text("(").color(Color.DARK_GRAY))
                    .append(Component.text(entry.getClaim().getDepth()).color(Color.DARK_PURPLE))
                    .append(Component.text(")").color(Color.DARK_GRAY))
                    .append(Component.text(" | ").color(Color.GRAY))
                    .append(Component.text(entry.getPriority().getName()).color(Color.YELLOW))
                    .append(Component.text(" | ").color(Color.GRAY))
                    .append(Component.text(entry.getPermission().asString()).color(Color.WHITE))
                    .append(Component.text(": ").color(Color.GRAY))
                    .append(entry.getValue().format())
                    .append(Component.text("\n"));
        }

        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Permission ").color(Color.YELLOW)
                .append(Component.text(permission.asString()).color(Color.ORANGE))
                .append(Component.text(" in ").color(Color.YELLOW))
                .append(Component.text(claim.getFullName()).color(Color.ORANGE))
                .append(Component.text(" is set to ").color(Color.YELLOW))
                .append(context.getValue().format().style(style -> style.hoverEvent(HoverEvent.showText(hover.build()))))
                .append(Component.text(" for ").color(Color.YELLOW))
                .append(Component.text(target.getName()).color(Color.ORANGE)));
        return 1;
    }

    public static int setPermission(ServerCommandSource source, AbstractClaim claim, GameProfile target, Permission permission, Permission.Value value) throws CommandSyntaxException {
        validatePermission(claim, source.getPlayer().getUuid(), "modify.permission");
        claim.getPermissionManager().setPlayerPermission(target.getId(), permission, value);
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Set permission ").color(Color.YELLOW)
                .append(Component.text(permission.asString())).color(Color.ORANGE)
                .append(Component.text(" for ")).color(Color.YELLOW)
                .append(Component.text(target.getName())).color(Color.ORANGE).append(Component.text(" to ")).color(Color.YELLOW).append(value.format()));
        return 0;
    }

    public static int listPermission(ServerCommandSource source, AbstractClaim claim, GameProfile target) throws CommandSyntaxException {
        validatePermission(claim, source.getPlayer().getUuid(), "modify.permission");
        TextComponent.Builder roleBuilder = Component.text();
        //TODO:
        /*for (Map.Entry<Role, Integer> entry : claim.getPermissionManager().getRolesByWeight(target.getId()).entrySet()) {
            roleBuilder.append(Component.text(ItsOursMod.INSTANCE.getRoleManager().getRoleID(entry.getKey()) + " (").color(Color.YELLOW))
                    .append(Component.text(String.valueOf(entry.getValue())).color(Color.ORANGE))
                    .append(Component.text(") ").color(Color.YELLOW));
        }*/

        TextComponent.Builder builder = Component.text().content("Player Info (").color(Color.ORANGE)
                .append(Component.text(target.getName()).color(Color.RED))
                .append(Component.text(")\n").color(Color.ORANGE))
                .append(InfoCommand.newInfoLine("Claim", Component.text(claim.getFullName()).color(Color.WHITE).clickEvent(ClickEvent.runCommand("/claim info " + claim.getFullName()))))
                .append(InfoCommand.newInfoLine("Roles", roleBuilder.build().clickEvent(ClickEvent.runCommand("/claim role " + claim.getFullName() + " list " + target.getName()))))
                .append(InfoCommand.newInfoLine("Permissions", claim.getPermissionManager().getPlayerPermission(target.getId()).toText()));

        ((ClaimPlayer) source.getPlayer()).sendMessage(builder.build());
        return 0;
    }

    public static int listPermissions(ServerCommandSource source) throws CommandSyntaxException {
        TextComponent.Builder builder = Component.text().content("Permissions:\n").color(Color.ORANGE);
        for (Node node : PermissionList.permission.getNodes()) {
            builder.append(Component.text(node.getId()).color(Color.LIGHT_GREEN), Component.text(": " + node.getInformation() + "\n").color(Color.LIGHT_GRAY));
        }
        ((ClaimPlayer) source.getPlayer()).sendMessage(builder.build());
        return 1;
    }

}
