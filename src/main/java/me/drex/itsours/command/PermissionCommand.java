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
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


public class PermissionCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = claimArgument();
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
        command.then(claim);
        literal.then(command);
    }

    public static int checkPlayer(ServerCommandSource source, AbstractClaim claim, GameProfile target, String permission) throws CommandSyntaxException {
        //TODO: Check if excutor is allowed to check
        boolean value = claim.getPermissionManager().hasPermission(target.getId(), permission).value;
        String perm = permission;
        TextComponent.Builder hover = Component.text();
        hover.append(checkPermission(claim.getPermissionManager(), target, permission));
        while (permission.contains(".")) {
            String[] node = permission.split("\\.");
            permission = permission.substring(0, (permission.length() - (node[node.length - 1]).length() - 1));
            hover.append(Component.text("\n"));
            hover.append(checkPermission(claim.getPermissionManager(), target, permission));
        }
        boolean value2 = claim.hasPermission(target.getId(), permission);
        if (value != value2) hover.append(Component.text("\n*Note: The actual value is ").color(Color.RED).append(Permission.Value.of(value2).format()).append(Component.text(", because of a parent claim.").color(Color.RED)));
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text(target.getName() + " (").color(Color.YELLOW)
                .append(Component.text(perm).color(Color.ORANGE))
                .append(Component.text("): ").color(Color.YELLOW))
                .append(Permission.Value.of(value).format().style(style -> style.hoverEvent(HoverEvent.showText(hover.build())))));
    return 1;
    }

    public static Component checkPermission(PermissionManager pm, GameProfile target, String permission) {
        AtomicReference<String> reference = new AtomicReference<>();
        TextComponent.Builder hover = Component.text()
                .append(Component.text(permission + ":").color(Color.PINK))
                .append(Component.text("\n -Settings: ").color(Color.YELLOW));
        hover.append(pm.settings.getPermission(permission, reference::set).format());
        appendOptionally(hover, permission, reference.get());

        TextComponent.Builder roles = Component.text().content("\n -Roles: ").color(Color.YELLOW);
        for (Map.Entry<Role, Integer> entry : pm.getRolesByWeight(target.getId()).entrySet()) {
            roles.append(Component.text("\n  *" + ItsOursMod.INSTANCE.getRoleManager().getRoleID(entry.getKey()) + " (").color(Color.YELLOW))
                    .append(Component.text(String.valueOf(entry.getValue())).color(Color.ORANGE))
                    .append(Component.text("): ").color(Color.YELLOW))
                    .append(entry.getKey().permissions().getPermission(permission, reference::set).format());
            appendOptionally(roles, permission, reference.get());
        }

        hover.append(roles);

        hover.append(Component.text("\n -Permissions: ").color(Color.YELLOW)
                .append((pm.playerPermission.get(target.getId()) != null ? pm.playerPermission.get(target.getId()).getPermission(permission, reference::set) : Permission.Value.UNSET).format()));
        appendOptionally(hover, permission, reference.get());
        return hover.build();
    }

    private static void appendOptionally(TextComponent.Builder builder, String permission, String permission2) {
        if (!permission.equals(permission2) && permission2 != null) {
            builder.append(Component.text(" (" + permission2 + ")").color(Color.WHITE).decorate(TextDecoration.ITALIC));
        }
    }

    public static int setPermission(ServerCommandSource source, AbstractClaim claim, GameProfile target, String permission, Permission.Value value) throws CommandSyntaxException {
        claim.getPermissionManager().setPlayerPermission(target.getId(), permission, value);
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Set permission ").color(Color.YELLOW)
                .append(Component.text(permission)).color(Color.ORANGE)
                .append(Component.text(" for ")).color(Color.YELLOW)
                .append(Component.text(target.getName())).color(Color.ORANGE).append(Component.text(" to ")).color(Color.YELLOW).append(value.format()));
        return 0;
    }

    public static int listPermission(ServerCommandSource source, AbstractClaim claim, GameProfile target) throws CommandSyntaxException {
        TextComponent.Builder roleBuilder = Component.text();
        for (Map.Entry<Role, Integer> entry : claim.getPermissionManager().getRolesByWeight(target.getId()).entrySet()) {
            roleBuilder.append(Component.text(ItsOursMod.INSTANCE.getRoleManager().getRoleID(entry.getKey()) + " (").color(Color.YELLOW))
                    .append(Component.text(String.valueOf(entry.getValue())).color(Color.ORANGE))
                    .append(Component.text(") ").color(Color.YELLOW));
        }

        TextComponent.Builder builder = Component.text().content("Player Info (").color(Color.ORANGE)
                .append(Component.text(target.getName()).color(Color.RED))
                .append(Component.text(")\n").color(Color.ORANGE))
                .append(InfoCommand.newInfoLine("Claim", Component.text(claim.getFullName()).color(Color.WHITE).clickEvent(ClickEvent.runCommand("/claim info " + claim.getFullName()))))
                .append(InfoCommand.newInfoLine("Roles", roleBuilder.build().clickEvent(ClickEvent.runCommand("/claim role " + claim.getFullName() + " list " + target.getName()))))
                .append(InfoCommand.newInfoLine("Permissions", claim.getPermissionManager().getPlayerPermission(target.getId()).toText()));

        ((ClaimPlayer) source.getPlayer()).sendMessage(builder.build());
        return 0;
    }

}
