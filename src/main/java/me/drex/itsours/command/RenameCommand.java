package me.drex.itsours.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.util.Modify;
import me.drex.itsours.command.argument.ClaimArgument;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;

public class RenameCommand extends AbstractCommand {

    public static final RenameCommand INSTANCE = new RenameCommand();

    private RenameCommand() {
        super("rename");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal
                .then(
                        ClaimArgument.ownClaims()
                                .then(
                                        argument("newName", StringArgumentType.string())
                                                .executes(ctx -> executeRename(ctx.getSource(), ClaimArgument.getClaim(ctx), StringArgumentType.getString(ctx, "newName")))
                                )
                );
    }

    private int executeRename(ServerCommandSource src, AbstractClaim claim, String newName) throws CommandSyntaxException {
        if (claim.getOwner().equals(src.getEntityOrThrow().getUuid()) || ItsOurs.hasPermission(src, "rename")) {
            if (AbstractClaim.isNameInvalid(newName)) throw ClaimArgument.INVALID_NAME;
            if (ClaimList.INSTANCE.getClaim(newName).isPresent()) throw ClaimArgument.NAME_TAKEN;
            String originalName = claim.getFullName();
            claim.setName(newName);
            src.sendFeedback(Text.translatable("text.itsours.commands.rename.success", originalName, newName), false);
            return 1;
        } else {
            src.sendError(Text.translatable("text.itsours.commands.rename.error"));
            return -1;
        }
    }

}
