package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.util.Modify;
import me.drex.itsours.claim.permission.visitor.PermissionVisitorImpl;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.command.argument.PermissionArgument;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;

public class CheckCommand extends AbstractCommand {

    public static final CheckCommand INSTANCE = new CheckCommand();

    private CheckCommand() {
        super("check");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = ClaimArgument.ownClaims().then(
                argument("targets", GameProfileArgumentType.gameProfile()).then(
                        PermissionArgument.permission()
                                .executes(ctx -> execute(ctx.getSource(), ClaimArgument.getClaim(ctx), GameProfileArgumentType.getProfileArgument(ctx, "targets"), PermissionArgument.getPermission(ctx)))
                )
        );
        literal.then(claim);
    }

    private int execute(ServerCommandSource src, AbstractClaim claim, Collection<GameProfile> targets, Permission permission) throws CommandSyntaxException {
        validatePermission(src, claim, PermissionManager.MODIFY, Modify.CHECK.buildNode());
        for (GameProfile target : targets) {
            PermissionVisitorImpl visitor = new PermissionVisitorImpl();
            claim.visit(target.getId(), permission, visitor);
            List<PermissionVisitorImpl.Entry> entries = visitor.getEntries();
            AbstractClaim currentClaim = null;
            for (PermissionVisitorImpl.Entry entry : entries) {
                if (currentClaim != entry.claim()) {
                    currentClaim = entry.claim();
                    src.sendFeedback(Text.literal(entry.claim().getFullName()).formatted(Formatting.GRAY), false);
                }
                src.sendFeedback(Text.translatable("text.itsours.commands.check.entry", entry.context().toText(), entry.permission().asString(), entry.value().format()), false);
            }
            src.sendFeedback(Text.translatable("text.itsours.commands.check.result", visitor.getResult().format()), false);
        }
        return 1;
    }

}
