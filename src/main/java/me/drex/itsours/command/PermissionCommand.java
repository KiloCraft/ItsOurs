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
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

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
        MutableText hover = new LiteralText("");
        PermissionManager pm = claim.getPermissionManager();
        hover.append(new LiteralText("Settings: ").formatted(Formatting.YELLOW).append(pm.settings.isPermissionSet(permission) ? format(pm.settings.getPermission(permission)) : new LiteralText("unset").formatted(Formatting.GRAY)));

        MutableText roles = new LiteralText("\nRoles: ").formatted(Formatting.YELLOW);
        for (Role role : pm.getRolesByWeight(target.getId())) {
            roles.append(new LiteralText("\n *" + ItsOursMod.INSTANCE.getRoleManager().getRoleID(role) + " (").formatted(Formatting.YELLOW))
            .append(new LiteralText(String.valueOf(pm.roles.get(target.getId()).get(role))).formatted(Formatting.GOLD)
            .append(new LiteralText( "): ").formatted(Formatting.YELLOW))
            .append(role.permissions().isPermissionSet(permission) ? format(role.permissions().getPermission(permission)) : new LiteralText("unset").formatted(Formatting.GRAY)));
        }
        hover.append(roles);

        hover.append(new LiteralText("\nPermissions: ").formatted(Formatting.YELLOW).append(pm.isPlayerPermissionSet(target.getId(), permission) ? format(pm.playerPermission.get(target.getId()).getPermission(permission)) : new LiteralText("unset").formatted(Formatting.GRAY)));
        ((ClaimPlayer) source.getPlayer()).sendMessage(new LiteralText(target.getName() + " (").formatted(Formatting.YELLOW)
                .append(new LiteralText(permission).formatted(Formatting.GOLD))
                .append(new LiteralText("): ").formatted(Formatting.YELLOW)
                .append(format(claim.hasPermission(target.getId(), permission))).styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)))));
        return 1;
    }

    public int setPermission(ServerCommandSource source, AbstractClaim claim, GameProfile target, String permission, boolean value) throws CommandSyntaxException {
        claim.getPermissionManager().setPlayerPermission(target.getId(), permission, value);
        ((ClaimPlayer) source.getPlayer()).sendMessage(new LiteralText("Set permission ").formatted(Formatting.YELLOW)
                .append(permission).formatted(Formatting.GOLD)
                .append(" for ").formatted(Formatting.YELLOW)
                .append(target.getName()).formatted(Formatting.GOLD).append(" to ").formatted(Formatting.YELLOW).append(format(value)));
        return 0;
    }

    public int resetPermission(ServerCommandSource source, AbstractClaim claim, GameProfile target, String permission) throws CommandSyntaxException {
        claim.getPermissionManager().resetPlayerPermission(target.getId(), permission);
        ((ClaimPlayer) source.getPlayer()).sendMessage(new LiteralText("Reset permission ").formatted(Formatting.YELLOW)
                .append(permission).formatted(Formatting.GOLD)
                .append(" for ").formatted(Formatting.YELLOW)
                .append(target.getName()));
        return 0;
    }

}
