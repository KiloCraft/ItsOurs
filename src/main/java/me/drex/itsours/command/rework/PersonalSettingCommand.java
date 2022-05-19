package me.drex.itsours.command.rework;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.rework.PermissionInterface;
import me.drex.itsours.claim.permission.rework.Value;
import me.drex.itsours.claim.permission.rework.context.GlobalContext;
import me.drex.itsours.claim.permission.rework.context.PersonalContext;
import me.drex.itsours.claim.permission.rework.node.Node;
import me.drex.itsours.command.rework.argument.ClaimArgument;
import me.drex.itsours.command.rework.argument.PermissionArgument;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PersonalSettingCommand extends AbstractCommand {

    public static final PersonalSettingCommand INSTANCE = new PersonalSettingCommand();

    public PersonalSettingCommand() {
        super("personalSetting");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(ClaimArgument.ownClaims().then(
                        literal("check").then(
                                argument("targets", GameProfileArgumentType.gameProfile()).then(
                                        PermissionArgument.permission()
                                                .executes(ctx -> executeCheck(ctx.getSource(), ClaimArgument.getClaim(ctx), GameProfileArgumentType.getProfileArgument(ctx, "targets"), PermissionArgument.getPermission(ctx)))
                                )
                        )
                ).then(
                        literal("set").then(
                                argument("targets", GameProfileArgumentType.gameProfile()).then(
                                        PermissionArgument.permission().then(
                                                PermissionArgument.value()
                                                        .executes(ctx -> executeSet(ctx.getSource(), ClaimArgument.getClaim(ctx), GameProfileArgumentType.getProfileArgument(ctx, "targets"), PermissionArgument.getPermission(ctx), PermissionArgument.getValue(ctx)))
                                        )
                                )
                        )
                ).then(
                        literal("unset").then(
                                argument("targets", GameProfileArgumentType.gameProfile()).then(
                                        PermissionArgument.permission()
                                                .executes(ctx -> executeSet(ctx.getSource(), ClaimArgument.getClaim(ctx), GameProfileArgumentType.getProfileArgument(ctx, "targets"), PermissionArgument.getPermission(ctx), Value.UNSET))
                                )
                        )
                )
        );
    }

    private int executeSet(ServerCommandSource src, AbstractClaim claim, Collection<GameProfile> targets, PermissionInterface permission, Value value) throws CommandSyntaxException {
        // TODO: Check if executor has permission to change permission
        for (Node node : permission.getNodes()) {
            // TODO: Add to other contexts
            if (!node.canChange(new Node.ChangeContext(claim, new PersonalContext(src.getPlayer().getUuid()), value, src))) throw PermissionArgument.FORBIDDEN;
        }
        for (GameProfile target : targets) {
            claim.getPermissionManager().setPermission(target.getId(), permission, value);
            src.sendFeedback(Text.translatable("text.itsours.commands.personalSetting.set",
                    permission.asString(), Texts.toText(target), claim.getFullName(), value.format()
            ), false);
        }
        return 1;
    }

    private int executeCheck(ServerCommandSource src, AbstractClaim claim, Collection<GameProfile> targets, PermissionInterface permission) {
        // TODO: Check if executor has permission to check permission
        for (GameProfile target : targets) {
            Value value = claim.getPermissionManager().getPermission(target.getId(), permission);
            src.sendFeedback(Text.translatable("text.itsours.commands.personalSetting.check", permission.asString(), claim.getFullName(), value.format(), Texts.toText(target)), false);
        }
        return 1;
    }

}
