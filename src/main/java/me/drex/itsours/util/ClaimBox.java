package me.drex.itsours.util;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
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

    public static ClaimBox create(Vec3i first, Vec3i second) {
        return new ClaimBox(Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()), Math.min(first.getZ(), second.getZ()), Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()), Math.max(first.getZ(), second.getZ()));
    }

    private ClaimBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
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

    public void save(NbtCompound nbtCompound) {
        nbtCompound.put("min", toNbt(getMin()));
        nbtCompound.put("max", toNbt(getMax()));
    }

    public static ClaimBox load(NbtCompound nbtCompound) {
        return ClaimBox.create(fromNbt(nbtCompound.getCompound("min")), fromNbt(nbtCompound.getCompound("max")));
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

    public void drawOutline(ServerPlayerEntity player, BlockState blockState) {
        drawOutline(player, blockState, blockState);
    }

    public void drawOutline(ServerPlayerEntity player, BlockState blockState, BlockState center) {
        for (int x = 0; x < this.getBlockCountX(); x++) {
            boolean lineCenter = isCenterBlock(this.getBlockCountX(), x);
            BlockState state = lineCenter ? center : blockState;
            sendFakeBlock(player, x + this.getMinX(), this.getMinZ(), state);
            sendFakeBlock(player, x + this.getMinX(), this.getMaxZ(), state);
        }
        for (int z = 0; z < this.getBlockCountZ(); z++) {
            boolean lineCenter = isCenterBlock(this.getBlockCountZ(), z);
            BlockState state = lineCenter ? center : blockState;
            sendFakeBlock(player, this.getMinX(), z + this.getMinZ(), state);
            sendFakeBlock(player, this.getMaxX(), z + this.getMinZ(), state);
        }
    }

    private boolean isCenterBlock(int length, int current) {
        int center = length / 2;
        if (length % 2 == 0) {
            return center == current || center == current + 1;
        } else {
            return center == current;
        }
    }

    private void sendFakeBlock(ServerPlayerEntity player, int x, int z, @Nullable BlockState state) {
        ServerWorld world = player.getWorld();
        if (!world.isPosLoaded(x, z)) return;
        BlockPos pos = getY(world, x, player.getBlockY(), z).down();
        BlockUpdateS2CPacket packet;
        packet = state == null ? new BlockUpdateS2CPacket(player.getEntityWorld(), pos) : new BlockUpdateS2CPacket(pos, state);
        player.networkHandler.sendPacket(packet);
    }

    private static BlockPos getY(BlockView blockView, int x, int y, int z) {
        BlockPos blockPos = new BlockPos(x, y + 10, z);

        do {
            blockPos = blockPos.down();
            if (blockPos.getY() < 1) {
                return new BlockPos(x, y, z);
            }
        } while (!blockView.getBlockState(blockPos).isFullCube(blockView, blockPos));
        return blockPos.up();
    }

    private static BlockPos fromNbt(NbtCompound nbtCompound) {
        int x = nbtCompound.getInt("x");
        int y = nbtCompound.getInt("y");
        int z = nbtCompound.getInt("z");
        return new BlockPos(x, y, z);
    }

    private static NbtCompound toNbt(BlockPos blockPos) {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putInt("x", blockPos.getX());
        nbtCompound.putInt("y", blockPos.getY());
        nbtCompound.putInt("z", blockPos.getZ());
        return nbtCompound;
    }

}
