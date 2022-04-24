package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import net.minecraft.text.Text;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;

public class SetOwnerCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> newOwner = players();
        newOwner.executes(SetOwnerCommand::setOwner);
        RequiredArgumentBuilder<ServerCommandSource, String> claim = allClaimArgument();
        claim.then(newOwner);
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("setowner");
        command.requires(src -> hasPermission(src, "itsours.setowner"));
        command.then(claim);
        literal.then(command);
    }

    public static int setOwner(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        AbstractClaim claim = getClaim(ctx);
        getGameProfile(ctx).thenAccept(optional -> {
           optional.ifPresent(gameProfile -> {
               claim.setOwner(gameProfile.getId());
               // TODO:
               //ItsOurs.INSTANCE.getClaimList().update();
               ctx.getSource().sendFeedback(Text.translatable("text.itsours.command.set_owner",
                       claim.getName(),
                       gameProfile
               ), false);
           });
        });
        return 1;
    }

}

