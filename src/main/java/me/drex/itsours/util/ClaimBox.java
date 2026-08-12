package me.drex.itsours.util;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

public class ClaimBox extends BoundingBox {

    public static final Codec<ClaimBox> CODEC = BoundingBox.CODEC.xmap(blockBox -> new ClaimBox(blockBox.minX(), blockBox.minY(), blockBox.minZ(), blockBox.maxX(), blockBox.maxY(), blockBox.maxZ()), claimBox -> new BoundingBox(claimBox.minX(), claimBox.minY(), claimBox.minZ(), claimBox.maxX(), claimBox.maxY(), claimBox.maxZ()));

    public ClaimBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static ClaimBox create(Vec3i first, Vec3i second) {
        return new ClaimBox(Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()), Math.min(first.getZ(), second.getZ()), Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()), Math.max(first.getZ(), second.getZ()));
    }

    public ClaimBox expand(Direction direction, int amount) {
        if (direction.getAxisDirection().equals(Direction.AxisDirection.POSITIVE)) {
            return ClaimBox.create(getMin(), getMax().relative(direction, amount));
        } else {
            return ClaimBox.create(getMin().relative(direction, amount), getMax());
        }
    }

    public ClaimBox intersection(ClaimBox other) {
        int minX = Math.max(this.minX(), other.minX());
        int minY = Math.max(this.minY(), other.minY());
        int minZ = Math.max(this.minZ(), other.minZ());
        int maxX = Math.min(this.maxX(), other.maxX());
        int maxY = Math.min(this.maxY(), other.maxY());
        int maxZ = Math.min(this.maxZ(), other.maxZ());
        return new ClaimBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public BlockPos getMin() {
        return new BlockPos(minX(), minY(), minZ());
    }

    public BlockPos getMax() {
        return new BlockPos(maxX(), maxY(), maxZ());
    }

    public long getArea() {
        long x = this.maxX() - this.minX() + 1;
        long z = this.maxZ() - this.minZ() + 1;
        return x * z;
    }

    public long getVolume() {
        long y = this.maxY() - this.minY() + 1;
        return getArea() * y;
    }

    public boolean contains(BoundingBox other) {
        return this.minX() <= other.minX() && this.minY() <= other.minY() && this.minZ() <= other.minZ() &&
            this.maxX() >= other.maxX() && this.maxY() >= other.maxY() && this.maxZ() >= other.maxZ();
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
