package me.drex.itsours.util;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class ClaimBox extends BlockBox {

    public static final Codec<ClaimBox> CODEC = BlockBox.CODEC.xmap(blockBox -> new ClaimBox(blockBox.getMinX(), blockBox.getMinY(), blockBox.getMinZ(), blockBox.getMaxX(), blockBox.getMaxY(), blockBox.getMaxZ()), claimBox -> new BlockBox(claimBox.getMinX(), claimBox.getMinY(), claimBox.getMinZ(), claimBox.getMaxX(), claimBox.getMaxY(), claimBox.getMaxZ()));

    public ClaimBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static ClaimBox create(Vec3i first, Vec3i second) {
        return new ClaimBox(Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()), Math.min(first.getZ(), second.getZ()), Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()), Math.max(first.getZ(), second.getZ()));
    }

    public ClaimBox expand(Direction direction, int amount) {
        if (direction.getDirection().equals(Direction.AxisDirection.POSITIVE)) {
            return ClaimBox.create(getMin(), getMax().offset(direction, amount));
        } else {
            return ClaimBox.create(getMin().offset(direction, amount), getMax());
        }
    }

    public ClaimBox intersection(ClaimBox other) {
        int minX = Math.max(this.getMinX(), other.getMinX());
        int minY = Math.max(this.getMinY(), other.getMinY());
        int minZ = Math.max(this.getMinZ(), other.getMinZ());
        int maxX = Math.min(this.getMaxX(), other.getMaxX());
        int maxY = Math.min(this.getMaxY(), other.getMaxY());
        int maxZ = Math.min(this.getMaxZ(), other.getMaxZ());
        return new ClaimBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public BlockPos getMin() {
        return new BlockPos(getMinX(), getMinY(), getMinZ());
    }

    public BlockPos getMax() {
        return new BlockPos(getMaxX(), getMaxY(), getMaxZ());
    }

    public int getArea() {
        return (this.getMaxX() - this.getMinX() + 1) * (this.getMaxZ() - this.getMinZ() + 1);
    }

    public int getVolume() {
        return getArea() * (this.getMaxY() - this.getMinY() + 1);
    }

    public boolean contains(BlockBox other) {
        return this.getMinX() <= other.getMinX() && this.getMinY() <= other.getMinY() && this.getMinZ() <= other.getMinZ() &&
            this.getMaxX() >= other.getMaxX() && this.getMaxY() >= other.getMaxY() && this.getMaxZ() >= other.getMaxZ();
    }

    public boolean isCenterBlock(int length, int current) {
        if (length <= 2) return false;
        int center = length / 2;
        if (length % 2 == 0) {
            return center == current || center == current + 1;
        } else {
            return center == current;
        }
    }

}
