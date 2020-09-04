package me.drex.itsours.claim;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.dimension.DimensionType;

import java.util.UUID;
import java.util.function.Consumer;

public class Claim extends AbstractClaim {

    public Claim(String name, UUID owner, BlockPos pos1, BlockPos pos2, DimensionType dimension, BlockPos tppos) {
        super(name, owner, pos1, pos2, dimension, tppos);
    }

    @Override
    public int getDepth() {
        return 0;
    }

    @Override
    public void canExpand(Direction direction, int amount, Consumer<ExpandResult> result) {

    }

    @Override
    public boolean isSubzone() {
        return false;
    }
}
