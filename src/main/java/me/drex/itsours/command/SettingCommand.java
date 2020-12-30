package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.util.Group;
import me.drex.itsours.claim.permission.util.Permission;
import me.drex.itsours.claim.permission.util.Setting;
import me.drex.itsours.claim.permission.util.node.AbstractNode;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collections;
import java.util.List;

public class SettingCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = ownClaimArgument();
        {
            RequiredArgumentBuilder<ServerCommandSource, String> setting = settingArgument();
            setting.executes(ctx -> checkSetting(ctx.getSource(), getClaim(ctx), getSetting(ctx)));
            LiteralArgumentBuilder<ServerCommandSource> check = LiteralArgumentBuilder.literal("check");
            check.then(setting);
            claim.then(check);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, String> value = permissionValueArgument();
            value.executes(ctx -> setSetting(ctx.getSource(), getClaim(ctx), getSetting(ctx), getPermissionValue(ctx)));
            RequiredArgumentBuilder<ServerCommandSource, String> setting = settingArgument();
            LiteralArgumentBuilder<ServerCommandSource> set = LiteralArgumentBuilder.literal("set");
            setting.then(value);
            set.then(setting);
            claim.then(set);
        }
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("setting");
        command.executes(ctx -> listSettings(ctx.getSource()));
        command.then(claim);
        literal.then(command);
    }

    public static int listSettings(ServerCommandSource source) throws CommandSyntaxException {
        TextComponent.Builder builder = Component.text().content("Permissions:\n").color(Color.ORANGE);
        for (Permission permission : Permission.permissions) {
            if (!(permission instanceof Setting)) continue;
            builder.append(Component.text(permission.id).color(Color.LIGHT_GREEN), Component.text(": " + permission.information + "\n").color(Color.LIGHT_GRAY));
        }
        ((ClaimPlayer) source.getPlayer()).sendMessage(builder.build());
        return 1;
    }

    public static int checkSetting(ServerCommandSource source, AbstractClaim claim, String setting) throws CommandSyntaxException {
        validatePermission(claim, source.getPlayer().getUuid(), "modify.setting");
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Setting (").color(Color.ORANGE)
                .append(Component.text(setting).color(Color.YELLOW))
                .append(Component.text("): ").color(Color.ORANGE))
                .append((claim.getPermissionManager().settings.getPermission(setting).format())));
        return 1;
    }

    public static int setSetting(ServerCommandSource source, AbstractClaim claim, String setting, Permission.Value value) throws CommandSyntaxException {
        validatePermission(claim, source.getPlayer().getUuid(), "modify.setting");
        claim.getPermissionManager().settings.setPermission(setting, value);
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Set setting ").color(Color.YELLOW)
                .append(Component.text(setting)).color(Color.ORANGE)
                .append(Component.text(" to ")).color(Color.YELLOW).append(value.format()));
        return 0;
    }

}
