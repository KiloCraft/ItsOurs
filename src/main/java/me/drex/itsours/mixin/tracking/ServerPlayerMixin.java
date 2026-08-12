package me.drex.itsours.mixin.tracking;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.user.ClaimTrackingPlayer;
import me.drex.itsours.util.ClaimBox;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.BundlerInfo;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;
import java.util.function.Predicate;

import static me.drex.itsours.claim.AbstractClaim.SHOW_BLOCKS;
import static me.drex.itsours.claim.AbstractClaim.SHOW_BLOCKS_CENTER;
import static net.minecraft.network.protocol.BundlerInfo.BUNDLE_SIZE_LIMIT;
import static net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR;

@Unique
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements ClaimTrackingPlayer {

    public ServerPlayerMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    private static final Predicate<BlockState> BLOCKS_MOVEMENT = BlockBehaviour.BlockStateBase::blocksMotion;

    @Shadow
    public ServerGamePacketListenerImpl connection;

    @Shadow
    public abstract ChunkTrackingView getChunkTrackingView();

    @Shadow public abstract ServerLevel level();

    private final Long2ObjectMap<Set<BlockPos>> chunk2TrackedShowBlocks = new Long2ObjectArrayMap<>();

    private final Set<BlockPos> trackedShowBlocks = new HashSet<>();

    private final Set<AbstractClaim> trackedClaims = new HashSet<>();

    private final List<Packet<? super ClientGamePacketListener>> packets = new LinkedList<>();

    private boolean tracking = false;

    @Override
    public void addChunkBatch(List<LevelChunk> chunkBatch) {
        if (!tracking) return;
        for (LevelChunk chunk : chunkBatch) {
            showChunk(chunk.getPos());
        }
        sendBundlePackets();
    }

    private void showChunk(ChunkPos chunkPos) {
        List<AbstractClaim> claims = ClaimList.getIntersectingClaims(level(), new ClaimBox(chunkPos.getMinBlockX(), -Integer.MAX_VALUE, chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), Integer.MAX_VALUE, chunkPos.getMaxBlockZ()));
        trackedClaims.addAll(claims);
        for (AbstractClaim claim : claims) {
            showChunk(claim, chunkPos);
        }
    }

    private void showChunk(AbstractClaim claim, ChunkPos pos) {
        BlockState blockState = SHOW_BLOCKS[Math.min(claim.getDepth(), SHOW_BLOCKS.length - 1)].defaultBlockState();
        BlockState centerBlockState = SHOW_BLOCKS_CENTER[Math.min(claim.getDepth(), SHOW_BLOCKS_CENTER.length - 1)].defaultBlockState();

        ClaimBox box = claim.getBox();
        for (int x = Math.max(box.minX(), pos.getMinBlockX()); x < Math.min(box.minX() + box.getXSpan(), pos.getMaxBlockX() + 1); x++) {
            boolean lineCenter = box.isCenterBlock(box.getXSpan(), x - box.minX());
            BlockState state = lineCenter ? centerBlockState : blockState;

            sendFakeBlockIfInChunk(x, box.minZ(), pos, state);
            sendFakeBlockIfInChunk(x, box.maxZ(), pos, state);
        }
        for (int z = Math.max(box.minZ(), pos.getMinBlockZ()); z < Math.min(box.minZ() + box.getZSpan(), pos.getMaxBlockZ() + 1); z++) {
            boolean lineCenter = box.isCenterBlock(box.getZSpan(), z - box.minZ());
            BlockState state = lineCenter ? centerBlockState : blockState;

            sendFakeBlockIfInChunk(box.minX(), z, pos, state);
            sendFakeBlockIfInChunk(box.maxX(), z, pos, state);
        }
    }

    private void sendFakeBlockIfInChunk(int x, int z, ChunkPos pos, BlockState blockState) {
        if (x >= pos.getMinBlockX() && x <= pos.getMaxBlockX() && z >= pos.getMinBlockZ() && z <= pos.getMaxBlockZ()) {
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
        getChunkTrackingView().forEach(this::showChunk);
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
            packets.add(new ClientboundBlockUpdatePacket(level(), blockPos));
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
                getChunkTrackingView().forEach(chunkPos -> {
                    if (claim.getBox().intersects(chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), chunkPos.getMaxBlockZ())) {
                        trackedClaims.add(claim);
                        showChunk(claim, chunkPos);
                    }
                });
                sendBundlePackets();
            }
        }
    }

    private void sendBundlePackets() {
        int bundlePackets = Mth.positiveCeilDiv(packets.size(), BUNDLE_SIZE_LIMIT);
        for (int i = 0; i < bundlePackets; i++) {
            int fromIndex = BUNDLE_SIZE_LIMIT * i;
            int toIndex = Math.min(packets.size(), BUNDLE_SIZE_LIMIT * (i + 1));
            connection.send(new ClientboundBundlePacket(new LinkedList<>(packets.subList(fromIndex, toIndex))));
        }
        packets.clear();
    }

    private void sendFakeBlock(int x, int z, BlockState state) {
        Level world = level();
        int y = world.getHeight(OCEAN_FLOOR, x, z);
        int playerY = (int) getY();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y - 1, z);
        // Special handling for cave-like scenarios
        if (y - playerY > 16) {
            pos.setY(playerY + 16);
            if (BLOCKS_MOVEMENT.test(world.getBlockState(pos))) {
                while (BLOCKS_MOVEMENT.test(world.getBlockState(pos)) && pos.getY() > world.getMinY()) {
                    pos.move(0, -1, 0);
                }
            }
            while (!BLOCKS_MOVEMENT.test(world.getBlockState(pos)) && pos.getY() > world.getMinY()) {
                pos.move(0, -1, 0);
            }
        }

        packets.add(new ClientboundBlockUpdatePacket(pos, state));

        long chunkPos = ChunkPos.asLong(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        chunk2TrackedShowBlocks.computeIfAbsent(chunkPos, l -> new HashSet<>());
        chunk2TrackedShowBlocks.get(chunkPos).add(pos);
        trackedShowBlocks.add(pos);
    }

}
