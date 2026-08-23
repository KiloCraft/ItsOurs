package me.drex.itsours.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import java.util.Map;

import static me.drex.itsours.claim.AbstractClaim.isNameInvalid;
import static me.drex.itsours.util.LocalizedMessage.localized;
import static net.minecraft.commands.Commands.argument;

public class RenameCommand extends AbstractCommand {

    public static final RenameCommand INSTANCE = new RenameCommand();

    private RenameCommand() {
        super("rename");
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        literal
            .then(
                ClaimArgument.ownClaims()
                    .then(
                        argument("newName", StringArgumentType.string())
                            .executes(ctx -> executeRename(ctx.getSource(), ClaimArgument.getClaim(ctx), StringArgumentType.getString(ctx, "newName")))
                    )
            );
    }

    public int executeRename(CommandSourceStack src, AbstractClaim claim, String newName) throws CommandSyntaxException {
        if (claim.getOwner().equals(src.getEntityOrException().getUUID()) || ItsOurs.checkPermission(src, "itsours.rename", 2)) {
            if (isNameInvalid(newName)) throw ClaimArgument.INVALID_NAME;
            if (!claim.canRename(newName)) throw ClaimArgument.NAME_TAKEN;
            Map<String, Component> oldClaimPlaceholders = claim.placeholders(src.getServer(), "old_claim_");
            claim.setName(newName);
            src.sendSuccess(() -> localized("text.itsours.commands.rename.success", PlaceholderUtil.mergePlaceholderMaps(
                oldClaimPlaceholders,
                claim.placeholders(src.getServer())
            )), false);
            return 1;
        } else {
            throw MISSING_PERMISSION.create(src);
        }
    }

}
