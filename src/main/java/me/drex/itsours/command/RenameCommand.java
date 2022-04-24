package me.drex.itsours.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.Subzone;
import net.minecraft.text.Text;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

public class RenameCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> newOwner = RequiredArgumentBuilder.argument("newName", StringArgumentType.word());
        newOwner.executes(ctx -> rename(ctx.getSource(), getClaim(ctx), StringArgumentType.getString(ctx, "newName")));
        RequiredArgumentBuilder<ServerCommandSource, String> claim = permissionClaimArgument("modify.name");
        claim.then(newOwner);
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("rename");
        command.then(claim);
        literal.then(command);
    }

    public static int rename(ServerCommandSource source, AbstractClaim claim, String newName) throws CommandSyntaxException {
        validatePermission(claim, source, "modify.name");
        if (claim instanceof Subzone) {
            AbstractClaim parent = ((Subzone) claim).getParent();
            for (Subzone subzone : parent.getSubzones()) {
                if (subzone.getName().equals(newName))
                    throw new SimpleCommandExceptionType(Text.translatable("text.itsours.command.rename.already_taken")).create();
            }
        } else {
            if (ClaimList.INSTANCE.getClaim(newName).isPresent())
                throw new SimpleCommandExceptionType(Text.translatable("text.itsours.command.rename.already_taken")).create();
        }
        if (AbstractClaim.isNameInvalid(newName))
            throw new SimpleCommandExceptionType(Text.translatable("text.itsours.command.rename.invalid")).create();

        source.sendFeedback(Text.translatable("text.itsours.command.rename",
                Text.literal(claim.getName()).formatted(Formatting.GOLD), Text.literal(newName).formatted(Formatting.GOLD)
        ).formatted(Formatting.YELLOW), false);
        claim.setName(newName);
        return 1;
    }

}

