package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

public class InfoCommand extends Command {

    public void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("info");
        command.executes(context -> info(context.getSource()));
        literal.then(command);
    }

    public int info(ServerCommandSource source) throws CommandSyntaxException {
        BlockPos p = source.getPlayer().getBlockPos();
        AbstractClaim claim = ItsOursMod.INSTANCE.getClaimList().get(source.getWorld(), p);
        if (claim == null) {
            ((ClaimPlayer) source.getPlayer()).sendMessage(new LiteralText("Couldn't find a claim at your position!"));
        } else {
            ((ClaimPlayer) source.getPlayer()).sendMessage(new LiteralText("")
                    .append("Claim name: ")
                    .append(claim.getName())
                    .append("\nClaim Owner: ")
                    .append(String.valueOf(claim.getOwner())));
        }
        return 1;
    }
}