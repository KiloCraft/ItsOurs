package me.drex.itsours.claim;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.UUID;

public class Claim extends AbstractClaim {

    public Claim(String name, UUID owner, BlockPos pos1, BlockPos pos2, ServerWorld world, BlockPos tppos) {
        super(name, owner, pos1, pos2, world, tppos);
    }

    public Claim(CompoundTag tag) {
        super(tag);
    }

    @Override
    public int getDepth() {
        return 0;
    }

    @Override
    public int expand(UUID uuid, Direction direction, int amount) throws CommandSyntaxException {
        int previousArea = this.getArea();
        this.show(null);
        this.expand(direction, amount);
        int requiredBlocks = this.getArea() - previousArea;
        if (ItsOursMod.INSTANCE.getBlockManager().getBlocks(uuid) < requiredBlocks) {
            this.undoExpand(direction, amount);
            throw new SimpleCommandExceptionType(new LiteralText("You don't have enough claim blocks!")).create();
        }
        if (this.intersects()) {
            this.undoExpand(direction, amount);
            throw new SimpleCommandExceptionType(new LiteralText("You can't expand into other claims")).create();
        }
        if (this.max.getY() > 256 || this.min.getY() < 0) {
            this.undoExpand(direction, amount);
            throw new SimpleCommandExceptionType(new LiteralText("You can't expand outside of the world!")).create();
        }
        if (max.getX() - min.getX() > 1024 || max.getZ() - min.getZ() > 1024) {
            this.undoExpand(direction, amount);
            throw new SimpleCommandExceptionType(new LiteralText("You can't expand further than 1024 blocks")).create();
        }
        if (max.getX() < min.getX() || max.getY() < min.getY() || max.getZ() < min.getZ()) {
            this.undoExpand(direction, amount);
            throw new SimpleCommandExceptionType(new LiteralText("You can't shrink your claim that much")).create();
        }
        for (Subzone subzone : this.getSubzones()) {
            if (!subzone.isInside()) {
                this.undoExpand(direction, amount);
                throw new SimpleCommandExceptionType(new LiteralText("Shrinking would result in " + subzone.getName() + " being outside of " + this.getName())).create();
            }
        }
        this.show(Blocks.GOLD_BLOCK.getDefaultState());
        ItsOursMod.INSTANCE.getClaimList().update();
        return requiredBlocks;
    }

    private void undoExpand(Direction direction, int amount) {
        this.expand(direction, -amount);
        for (ServerPlayerEntity player : ItsOursMod.server.getPlayerManager().getPlayerList()) {
            ClaimPlayer claimPlayer = (ClaimPlayer) player;
            if (claimPlayer.getLastShowClaim() == this) {
                this.show(player, Blocks.GOLD_BLOCK.getDefaultState());
            }
        }
    }
}
