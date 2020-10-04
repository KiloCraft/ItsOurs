package me.drex.itsours.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class CreateCommand extends Command {

    public void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> name = RequiredArgumentBuilder.argument("name", StringArgumentType.word());
        name.executes(ctx -> create(ctx.getSource(), StringArgumentType.getString(ctx, "name")));
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("create");
        command.executes(ctx -> create(ctx.getSource(), ctx.getSource().getPlayer().getEntityName()));
        command.then(name);
        literal.then(command);
    }

    public int create(ServerCommandSource source, String name) throws CommandSyntaxException {
        ClaimPlayer claimPlayer = (ClaimPlayer) source.getPlayer();
        if (claimPlayer.arePositionsSet()) {
            BlockPos min = new BlockPos(claimPlayer.getLeftPosition());
            min = new BlockPos(min.getX(), 1, min.getZ());
            BlockPos max = new BlockPos(claimPlayer.getRightPosition());
            max = new BlockPos(max.getX(), 256, max.getZ());
            if (!AbstractClaim.NAME.matcher(name).matches()) throw new SimpleCommandExceptionType(new LiteralText("Claim name is to long or contains invalid characters")).create();
            if (ItsOursMod.INSTANCE.getClaimList().contains(name)) throw new SimpleCommandExceptionType(new LiteralText("Claim name is already taken")).create();
            Claim claim = new Claim(name, source.getPlayer().getUuid(), min, max, source.getWorld(), null);
            if (claim.intersects()) {
                //check if claim is completely inside on another one => create subzone
                //check for name again

            } else {
                if (ItsOursMod.INSTANCE.getBlockManager().getBlocks(source.getPlayer().getUuid()) < claim.getArea()) throw new SimpleCommandExceptionType(new LiteralText("You don't have enough claim blocks")).create();
                ((ClaimPlayer) source.getPlayer()).sendMessage(new LiteralText("Claim created!").formatted(Formatting.GREEN));
                if (claimPlayer.getLastShowClaim() != null) claimPlayer.getLastShowClaim().show(source.getPlayer(), null);
                claimPlayer.setLastShow(claim, source.getPlayer().getBlockPos(), source.getWorld());
                claim.show(source.getPlayer(), Blocks.GOLD_BLOCK.getDefaultState());
                ItsOursMod.INSTANCE.getClaimList().add(claim);
            }


            //reset positions
            claimPlayer.setLeftPosition(null);
            claimPlayer.setRightPosition(null);
            return 1;
        } else {
            return 0;
        }

    }
}
