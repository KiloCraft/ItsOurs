package me.drex.itsours.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public abstract class Command {

    AbstractClaim getAndValidateClaim(ServerWorld world, BlockPos pos) throws CommandSyntaxException {
        AbstractClaim claim = ItsOursMod.INSTANCE.getClaimList().get(world, pos);
        if (claim == null) throw new SimpleCommandExceptionType(new LiteralText("Couldn't find a claim at your position!")).create();
        return claim;
    }

    boolean hasPermission(ServerCommandSource src, String permission) {
        return ItsOursMod.INSTANCE.getPermissionHandler().hasPermission(src, permission, 2);
    }

}
