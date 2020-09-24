package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ShowCommand extends Command {

    public void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("show");
        command.executes(this::show);
        literal.then(command);
    }

    public int show(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        AbstractClaim claim = this.getAndValidateClaim(source.getWorld(), source.getPlayer().getBlockPos());
        ServerPlayerEntity player = source.getPlayer();
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        if (claimPlayer.getLastShowClaim() != null) claimPlayer.getLastShowClaim().show(player, null);
        claimPlayer.setLastShow(claim, source.getPlayer().getBlockPos(), source.getWorld());
        claim.show(player, Blocks.GOLD_BLOCK.getDefaultState());
        return 1;
    }

}
