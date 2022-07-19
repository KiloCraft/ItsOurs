package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.context.PersonalContext;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.claim.permission.util.Modify;
import me.drex.itsours.claim.permission.util.Value;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.command.argument.PermissionArgument;
import me.drex.itsours.util.Components;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class PermissionsCommand extends AbstractCommand {

    public static final PermissionsCommand INSTANCE = new PermissionsCommand();

    public PermissionsCommand() {
        super("permissions");
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

    public int executeSet(ServerCommandSource src, AbstractClaim claim, Collection<GameProfile> targets, Permission permission, Value value) throws CommandSyntaxException {
        validatePermission(src, claim, PermissionManager.MODIFY, Modify.PERMISSION.buildNode());
        permission.validateContext(new Node.ChangeContext(claim, new PersonalContext(src.getPlayer().getUuid()), value, src));
        for (GameProfile target : targets) {
            claim.getPermissionHolder().setPermission(target.getId(), permission, value);
            src.sendFeedback(Text.translatable("text.itsours.commands.personalSetting.set",
                    permission.asString(), Components.toText(target), claim.getFullName(), value.format()
            ), false);
        }
        return 1;
    }

    private int executeCheck(ServerCommandSource src, AbstractClaim claim, Collection<GameProfile> targets, Permission permission) throws CommandSyntaxException {
        validatePermission(src, claim, PermissionManager.MODIFY, Modify.PERMISSION.buildNode());
        for (GameProfile target : targets) {
            Value value = claim.getPermissionHolder().getPermission(target.getId(), permission);
            src.sendFeedback(Text.translatable("text.itsours.commands.personalSetting.check", permission.asString(), claim.getFullName(), value.format(), Components.toText(target)), false);
        }
        return 1;
    }

}
