package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.util.Modify;
import me.drex.itsours.claim.permission.visitor.Entry;
import me.drex.itsours.claim.permission.visitor.PermissionVisitor;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.command.argument.PermissionArgument;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import java.util.*;
import java.util.stream.Collectors;

import static me.drex.message.api.LocalizedMessage.localized;
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
        validatePermission(src, claim, PermissionManager.MODIFY, Modify.CHECK.node());
        for (GameProfile target : targets) {
            PermissionVisitor visitor = PermissionVisitor.create();
            claim.visit(target.getId(), permission, visitor);
            LinkedHashMap<AbstractClaim, List<Entry>> map = visitor.getEntries().stream()
                .collect(Collectors.groupingBy(Entry::claim, LinkedHashMap::new, Collectors.toList()));
            src.sendFeedback(() -> localized("text.itsours.commands.check", Map.of(
                "list", PlaceholderUtil.list(map.entrySet(), claimCheckResult -> {
                    return PlaceholderUtil.mergePlaceholderMaps(
                        claimCheckResult.getKey().placeholders(src.getServer()),
                        new HashMap<>() {{
                            put("list", PlaceholderUtil.list(claimCheckResult.getValue(), entry -> {
                                return entry.placeholders(src.getServer());
                            }, "text.itsours.commands.check.list.list"));
                        }}
                    );
                }, "text.itsours.commands.check.list"),
                "value", visitor.getResult().format()
            )), false);
        }
        return 1;
    }

}
