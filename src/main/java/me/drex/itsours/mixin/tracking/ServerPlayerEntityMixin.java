package me.drex.itsours.mixin.tracking;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.user.ClaimTrackingPlayer;
import me.drex.itsours.util.ClaimBox;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.handler.PacketBundleHandler;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.server.network.ChunkFilter;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;
import java.util.function.Predicate;

import static me.drex.itsours.claim.AbstractClaim.SHOW_BLOCKS;
import static me.drex.itsours.claim.AbstractClaim.SHOW_BLOCKS_CENTER;
import static net.minecraft.network.handler.PacketBundleHandler.MAX_PACKETS;
import static net.minecraft.world.Heightmap.Type.OCEAN_FLOOR;

@Unique
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ClaimTrackingPlayer {

    public ServerPlayerEntityMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    private static final Predicate<BlockState> BLOCKS_MOVEMENT = AbstractBlock.AbstractBlockState::blocksMovement;

    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    @Shadow
    public abstract ChunkFilter getChunkFilter();

    @Shadow public abstract ServerWorld getEntityWorld();

    private final Long2ObjectMap<Set<BlockPos>> chunk2TrackedShowBlocks = new Long2ObjectArrayMap<>();

    private final Set<BlockPos> trackedShowBlocks = new HashSet<>();

    private final Set<AbstractClaim> trackedClaims = new HashSet<>();

    private final List<Packet<? super ClientPlayPacketListener>> packets = new LinkedList<>();

    private boolean tracking = false;

    @Override
    public void addChunkBatch(List<WorldChunk> chunkBatch) {
        if (!tracking) return;
        for (WorldChunk chunk : chunkBatch) {
            showChunk(chunk.getPos());
        }
        sendBundlePackets();
    }

    private void showChunk(ChunkPos chunkPos) {
        List<AbstractClaim> claims = ClaimList.getIntersectingClaims(getEntityWorld(), new ClaimBox(chunkPos.getStartX(), -Integer.MAX_VALUE, chunkPos.getStartZ(), chunkPos.getEndX(), Integer.MAX_VALUE, chunkPos.getEndZ()));
        trackedClaims.addAll(claims);
        for (AbstractClaim claim : claims) {
            showChunk(claim, chunkPos);
        }
    }

    private void showChunk(AbstractClaim claim, ChunkPos pos) {
        BlockState blockState = SHOW_BLOCKS[Math.min(claim.getDepth(), SHOW_BLOCKS.length - 1)].getDefaultState();
        BlockState centerBlockState = SHOW_BLOCKS_CENTER[Math.min(claim.getDepth(), SHOW_BLOCKS_CENTER.length - 1)].getDefaultState();

        ClaimBox box = claim.getBox();
        for (int x = Math.max(box.getMinX(), pos.getStartX()); x < Math.min(box.getMinX() + box.getBlockCountX(), pos.getEndX() + 1); x++) {
            boolean lineCenter = box.isCenterBlock(box.getBlockCountX(), x - box.getMinX());
            BlockState state = lineCenter ? centerBlockState : blockState;

            sendFakeBlockIfInChunk(x, box.getMinZ(), pos, state);
            sendFakeBlockIfInChunk(x, box.getMaxZ(), pos, state);
        }
        for (int z = Math.max(box.getMinZ(), pos.getStartZ()); z < Math.min(box.getMinZ() + box.getBlockCountZ(), pos.getEndZ() + 1); z++) {
            boolean lineCenter = box.isCenterBlock(box.getBlockCountZ(), z - box.getMinZ());
            BlockState state = lineCenter ? centerBlockState : blockState;

            sendFakeBlockIfInChunk(box.getMinX(), z, pos, state);
            sendFakeBlockIfInChunk(box.getMaxX(), z, pos, state);
        }
    }

    private void sendFakeBlockIfInChunk(int x, int z, ChunkPos pos, BlockState blockState) {
        if (x >= pos.getStartX() && x <= pos.getEndX() && z >= pos.getStartZ() && z <= pos.getEndZ()) {
            sendFakeBlock(x, z, blockState);
        }
    }

    @Override
    public boolean isTracked(BlockPos pos) {
        return trackedShowBlocks.contains(pos);
    }

    @Override
    public void onChunkUnload(ChunkPos pos) {
        Set<BlockPos> removedBlocks = chunk2TrackedShowBlocks.remove(pos.toLong());
        if (removedBlocks != null) {
            trackedShowBlocks.removeAll(removedBlocks);
        }
    }

    @Override
    public void trackClaims() {
        if (tracking) return;
        tracking = true;
        trackClaims0();
        sendBundlePackets();
    }

    private void trackClaims0() {
        getChunkFilter().forEach(this::showChunk);
    }

    @Override
    public void unTrackClaims() {
        if (!tracking) return;
        tracking = false;
        unTrackClaims0();
        sendBundlePackets();
    }

    private void unTrackClaims0() {
        for (BlockPos blockPos : trackedShowBlocks) {
            packets.add(new BlockUpdateS2CPacket(getEntityWorld(), blockPos));
        }
        chunk2TrackedShowBlocks.clear();
        trackedShowBlocks.clear();
    }

    @Override
    public void notifyChange(AbstractClaim claim, boolean add) {
        if (tracking) {
            if (trackedClaims.contains(claim)) {
                unTrackClaims0();
                trackClaims0();
                sendBundlePackets();
            } else if (add) {
                getChunkFilter().forEach(chunkPos -> {
                    if (claim.getBox().intersectsXZ(chunkPos.getStartX(), chunkPos.getStartZ(), chunkPos.getEndX(), chunkPos.getEndZ())) {
                        trackedClaims.add(claim);
                        showChunk(claim, chunkPos);
                    }
                });
                sendBundlePackets();
            }
        }
    }

    private void sendBundlePackets() {
        int bundlePackets = MathHelper.ceilDiv(packets.size(), MAX_PACKETS);
        for (int i = 0; i < bundlePackets; i++) {
            int fromIndex = MAX_PACKETS * i;
            int toIndex = Math.min(packets.size(), MAX_PACKETS * (i + 1));
            networkHandler.sendPacket(new BundleS2CPacket(new LinkedList<>(packets.subList(fromIndex, toIndex))));
        }
        packets.clear();
    }

    private void sendFakeBlock(int x, int z, BlockState state) {
        World world = getEntityWorld();
        int y = world.getTopY(OCEAN_FLOOR, x, z);
        int playerY = (int) getY();
        BlockPos.Mutable pos = new BlockPos.Mutable(x, y - 1, z);
        // Special handling for cave-like scenarios
        if (y - playerY > 16) {
            pos.setY(playerY + 16);
            if (BLOCKS_MOVEMENT.test(world.getBlockState(pos))) {
                while (BLOCKS_MOVEMENT.test(world.getBlockState(pos)) && pos.getY() > world.getBottomY()) {
                    pos.move(0, -1, 0);
                }
            }
            while (!BLOCKS_MOVEMENT.test(world.getBlockState(pos)) && pos.getY() > world.getBottomY()) {
                pos.move(0, -1, 0);
            }
        }

        packets.add(new BlockUpdateS2CPacket(pos, state));

        long chunkPos = ChunkPos.toLong(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
        chunk2TrackedShowBlocks.computeIfAbsent(chunkPos, l -> new HashSet<>());
        chunk2TrackedShowBlocks.get(chunkPos).add(pos);
        trackedShowBlocks.add(pos);
    }

}
