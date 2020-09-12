package me.drex.itsours.claim;

import com.sun.istack.internal.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.dimension.DimensionType;

import java.util.UUID;
import java.util.function.Consumer;

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

    @Override
    public void canExpand(Direction direction, int amount, Consumer<ExpandResult> result) {

    }

    public AbstractClaim getParent() {
        return this.parent;
    }

    public int getDepth() {
        return this.getParent().getDepth() + 1;
    }

    @Override
    public boolean isSubzone() {
        return true;
    }
}
