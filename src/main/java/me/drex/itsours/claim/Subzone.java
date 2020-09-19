package me.drex.itsours.claim;

import com.sun.istack.internal.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.UUID;

public class Subzone extends AbstractClaim {

    final AbstractClaim parent;

    public Subzone(String name, UUID owner, BlockPos min, BlockPos max, ServerWorld world, @Nullable BlockPos tppos, AbstractClaim parent) {
        super(name, owner, min, max, world, tppos);
        //Make sure the parent isnt also in the subzone list (getDepth() would get an infinite loop)
        this.parent = parent;
    }

    public Subzone (CompoundTag tag, AbstractClaim parent) {
        super(tag);
        this.parent = parent;
    }

    public AbstractClaim getParent() {
        return this.parent;
    }

    public int getDepth() {
        return this.getParent().getDepth() + 1;
    }

    @Override
    int expand(UUID uuid, Direction direction, int amount) {
        return 0;
    }

    boolean isInside() {
        BlockPos a = min, b = max, c = new BlockPos(max.getX(), min.getY(), min.getZ()), d = new BlockPos(min.getX(), max.getY(), min.getZ()), e = new BlockPos(min.getX(), min.getY(), max.getZ()), f = new BlockPos(max.getX(), max.getY(), min.getZ()), g = new BlockPos(max.getX(), min.getY(), max.getZ()), h = new BlockPos(min.getX(), max.getY(), max.getZ());
        return this.parent.contains(a) && this.parent.contains(b) && this.parent.contains(c) && this.parent.contains(d) && this.parent.contains(e) && this.parent.contains(f) && this.parent.contains(g) && this.parent.contains(h);
    }

}
