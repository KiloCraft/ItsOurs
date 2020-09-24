package me.drex.itsours.claim;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.ItsOursMod;
import net.minecraft.nbt.CompoundTag;
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
        this.expand(direction, amount);
        int requiredBlocks = this.getArea() - previousArea;
        if (ItsOursMod.INSTANCE.getBlockManager().getBlocks(uuid) < requiredBlocks) {
            this.expand(direction, -amount);
            throw new SimpleCommandExceptionType(new LiteralText("You don't have enough claim blocks!")).create();
        }
        if (this.intersects()) {
            this.expand(direction, -amount);
            throw new SimpleCommandExceptionType(new LiteralText("Expansion would result in hitting another claim")).create();
        }
        if (this.max.getY() > 256 || this.min.getY() < 0) {
            this.expand(direction, -amount);
            throw new SimpleCommandExceptionType(new LiteralText("You can't expand outside of the world!")).create();
        }
        if (max.getX() - min.getX() > 1024 || max.getZ() - min.getZ() > 1024) {
            this.expand(direction, -amount);
            throw new SimpleCommandExceptionType(new LiteralText("Expansion would result in exceeding the maximum size of 1024")).create();

        }
        for (Subzone subzone : this.getSubzones()) {
            if (!subzone.isInside()) {
                this.expand(direction, -amount);
                throw new SimpleCommandExceptionType(new LiteralText("Shrinking would result in " + subzone.getName() + " being outside of " + this.getName())).create();
            }
        }
        ItsOursMod.INSTANCE.getClaimList().update();
        return requiredBlocks;
    }
}
