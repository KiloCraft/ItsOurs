package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.claim.permission.util.Permission;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.Arrays;

import static me.drex.itsours.util.TextUtil.format;

public class PermissionCommand extends Command {

    public void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = claimArgument();
        {
            //TODO: Add proper suggestion
            RequiredArgumentBuilder<ServerCommandSource, String> permission = RequiredArgumentBuilder.argument("perm", StringArgumentType.word());
            permission.executes(ctx -> checkPlayer(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), StringArgumentType.getString(ctx, "perm")));
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            LiteralArgumentBuilder<ServerCommandSource> check = LiteralArgumentBuilder.literal("check");
            player.then(permission);
            check.then(player);
            claim.then(check);
        }
        {
            //TODO: Add proper suggestion
            RequiredArgumentBuilder<ServerCommandSource, Boolean> value = RequiredArgumentBuilder.argument("value", BoolArgumentType.bool());
            value.executes(ctx -> setPermission(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), StringArgumentType.getString(ctx, "perm"), BoolArgumentType.getBool(ctx, "value")));
            RequiredArgumentBuilder<ServerCommandSource, String> permission = RequiredArgumentBuilder.argument("perm", StringArgumentType.word());
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            LiteralArgumentBuilder<ServerCommandSource> set = LiteralArgumentBuilder.literal("set");
            permission.then(value);
            player.then(permission);
            set.then(player);
            claim.then(set);
        }
        {
            //TODO: Add proper suggestion
            RequiredArgumentBuilder<ServerCommandSource, String> permission = RequiredArgumentBuilder.argument("perm", StringArgumentType.word());
            permission.executes(ctx -> resetPermission(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), StringArgumentType.getString(ctx, "perm")));
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            LiteralArgumentBuilder<ServerCommandSource> reset = LiteralArgumentBuilder.literal("reset");
            player.then(permission);
            reset.then(player);
            claim.then(reset);
        }
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("permission");
        command.then(claim);
        literal.then(command);
    }

    public int checkPlayer(ServerCommandSource source, AbstractClaim claim, GameProfile target, String permission) throws CommandSyntaxException {
        //TODO: Check if excutor is allowed to check
        boolean value = claim.hasPermission(target.getId(), permission);
        String perm = permission;
        TextComponent.Builder hover = Component.text();
        hover.append(checkPermission(claim.getPermissionManager(), target, permission));
        while (permission.contains(".")) {
            String[] node = permission.split("\\.");
            permission = permission.substring(0, (permission.length() - (node[node.length-1]).length() - 1));
            hover.append(Component.text("\n"));
            hover.append(checkPermission(claim.getPermissionManager(), target, permission));
        }
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text(target.getName() + " (").color(Color.YELLOW)
                .append(Component.text(perm).color(Color.ORANGE))
                .append(Component.text("): ").color(Color.YELLOW))
                .append(format(value).style(style -> style.hoverEvent(HoverEvent.showText(hover.build())))));
        return 1;
    }

    public Component checkPermission(PermissionManager pm, GameProfile target, String permission) {
        TextComponent.Builder hover = Component.text()
                .append(Component.text(permission + ":").color(Color.PINK))
                .append(Component.text("\n -Settings: ").color(Color.YELLOW))
                .append(pm.settings.isPermissionSet(permission) ? format(pm.settings.getPermission(permission)) : Component.text("unset").color(Color.GRAY));

        TextComponent.Builder roles = Component.text().content("\n -Roles: ").color(Color.YELLOW);
        for (Role role : pm.getRolesByWeight(target.getId())) {
            roles.append(Component.text("\n  *" + ItsOursMod.INSTANCE.getRoleManager().getRoleID(role) + " (").color(Color.YELLOW))
            .append(Component.text(String.valueOf(pm.roles.get(target.getId()).get(role))).color(Color.ORANGE))
            .append(Component.text("): ").color(Color.YELLOW))
            .append(role.permissions().isPermissionSet(permission) ? format(role.permissions().getPermission(permission)) : Component.text("unset").color(Color.GRAY));
        }

        hover.append(roles);

        hover.append(Component.text("\n -Permissions: ").color(Color.YELLOW)
            .append(pm.isPlayerPermissionSet(target.getId(), permission) ? format(pm.playerPermission.get(target.getId()).getPermission(permission)) : Component.text("unset").color(Color.GRAY)));
        return hover.build();
    }
    public int setPermission(ServerCommandSource source, AbstractClaim claim, GameProfile target, String permission, boolean value) throws CommandSyntaxException {
        claim.getPermissionManager().setPlayerPermission(target.getId(), permission, value);
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Set permission ").color(Color.YELLOW)
                .append(Component.text(permission)).color(Color.ORANGE)
                .append(Component.text(" for ")).color(Color.YELLOW)
                .append(Component.text(target.getName())).color(Color.ORANGE).append(Component.text(" to ")).color(Color.YELLOW).append(format(value)));
        return 0;
    }

    public int resetPermission(ServerCommandSource source, AbstractClaim claim, GameProfile target, String permission) throws CommandSyntaxException {
        claim.getPermissionManager().resetPlayerPermission(target.getId(), permission);
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Reset permission ").color(Color.YELLOW)
                .append(Component.text(permission)).color(Color.ORANGE)
                .append(Component.text(" for ")).color(Color.YELLOW)
                .append(Component.text(target.getName())));
        return 0;
    }

}
