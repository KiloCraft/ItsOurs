package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionList;
import me.drex.itsours.claim.permission.rework.Value;
import me.drex.itsours.claim.permission.util.node.util.Node;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

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

    public static int checkSetting(ServerCommandSource source, AbstractClaim claim, Permission setting) throws CommandSyntaxException {
        validatePermission(claim, source, "modify.setting");
        // TODO:
        /*source.sendFeedback(Text.translatable("text.itsours.command.setting.check",
                setting.asString(),
                claim.getFullName(),
                claim.getPermissionManager().settings.getPermission(claim, setting, Priority.SETTING).getValue().format()
        ), false);*/
        return 1;
    }

    public static int setSetting(ServerCommandSource source, AbstractClaim claim, Permission setting, Value value) throws CommandSyntaxException {
        validatePermission(claim, source, "modify.setting");
        // TODO:
        //claim.getPermissionManager().settings.setPermission(setting.asString(), value);
        source.sendFeedback(Text.translatable("text.itsours.command.setting.set", setting.asString(), claim.getFullName(), value.format()), false);
        return 0;
    }

    public static int listSettings(ServerCommandSource source) {
        source.sendFeedback(Text.translatable("text.itsours.command.setting.list"), false);
        for (Node node : PermissionList.INSTANCE.setting.getNodes()) {
            source.sendFeedback(Text.translatable("text.itsours.command.setting.list.entry", node.getId(), node.getInformation()), false);
        }
        return 1;
    }

}
