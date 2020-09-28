package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

public class CreateCommand extends Command {

    public void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("create");
        command.executes(context -> create(context.getSource()));
        literal.then(command);
    }

    public int create(ServerCommandSource source) throws CommandSyntaxException {
        BlockPos p = source.getPlayer().getBlockPos();
        BlockPos min = new BlockPos(p.getX() - 3, 1, p.getZ() - 3);
        BlockPos max = new BlockPos(p.getX() + 3, 256, p.getZ() + 3);
        Claim claim = new Claim(source.getPlayer().getEntityName(), source.getPlayer().getUuid(), min, max, source.getWorld(), null);
        //TODO: Check if claim is allowed at that position
        ItsOursMod.INSTANCE.getClaimList().add(claim);
        ((ClaimPlayer) source.getPlayer()).sendMessage(new LiteralText("Claim created!"));
        return 1;
    }
}
