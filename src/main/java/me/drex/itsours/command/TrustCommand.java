package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.holder.ClaimPermissionHolder;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.claim.permission.roles.RoleManager;
import me.drex.itsours.claim.permission.util.Modify;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.util.Components;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;

public class TrustCommand extends AbstractCommand {

    public static final TrustCommand TRUST = new TrustCommand("trust", true);
    public static final TrustCommand DISTRUST = new TrustCommand("distrust", false);

    private final boolean trust;

    private TrustCommand(@NotNull String literal, boolean trust) {
        super(literal);
        this.trust = trust;
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(
                ClaimArgument.ownClaims()
                        .then(
                                argument("targets", GameProfileArgumentType.gameProfile())
                                        .executes(ctx -> executeTrust(ctx.getSource(), ClaimArgument.getClaim(ctx), GameProfileArgumentType.getProfileArgument(ctx, "targets")))
                        )
        );
    }

    public int executeTrust(ServerCommandSource src, AbstractClaim claim, Collection<GameProfile> targets) throws CommandSyntaxException {
        validatePermission(src, claim, PermissionManager.MODIFY, (trust ? Modify.TRUST : Modify.DISTRUST).buildNode());
        ClaimPermissionHolder permissionHolder = claim.getPermissionHolder();
        Role trusted = RoleManager.INSTANCE.getRole(RoleManager.TRUSTED_ID);
        int result = 0;
        if (trust) {
            for (GameProfile target : targets) {
                if (permissionHolder.addRole(target.getId(), trusted)) {
                    src.sendFeedback(Text.translatable("text.itsours.commands.trust", Components.toText(target), claim.getName()), false);
                    result++;
                } else {
                    src.sendError(Text.translatable("text.itsours.commands.trust.nothingChanged", Components.toText(target)));
                }
            }
        } else {
            for (GameProfile target : targets) {
                if (permissionHolder.removeRole(target.getId(), trusted)) {
                    src.sendFeedback(Text.translatable("text.itsours.commands.distrust", Components.toText(target), claim.getName()), false);
                    result++;
                } else {
                    src.sendError(Text.translatable("text.itsours.commands.distrust.nothingChanged", Components.toText(target)));
                }
            }
        }
        return result;
    }


}
