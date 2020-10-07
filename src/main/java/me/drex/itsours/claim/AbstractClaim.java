package me.drex.itsours.claim;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.WorldUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import static me.drex.itsours.claim.AbstractClaim.Util.getPosOnGround;

public abstract class AbstractClaim {

    public static final Pattern NAME = Pattern.compile("\\w{3,16}");
    private static Block[] showBlocks = {Blocks.GOLD_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.EMERALD_BLOCK, Blocks.REDSTONE_BLOCK, Blocks.LAPIS_BLOCK};
    public BlockPos min, max, tp;
    private String name;
    private UUID owner;
    private ServerWorld world;
    private List<Subzone> subzoneList = new ArrayList<>();
    private Date created;
    private Date lastEdited;
    private PermissionManager permissionManager;

    public AbstractClaim(String name, UUID owner, BlockPos pos1, BlockPos pos2, ServerWorld world, BlockPos tp) {
        this.name = name;
        this.owner = owner;
        int x, y, z, mx, my, mz;
        x = Math.min(pos1.getX(), pos2.getX());
        mx = Math.max(pos1.getX(), pos2.getX());
        y = Math.min(pos1.getY(), pos2.getY());
        my = Math.max(pos1.getY(), pos2.getY());
        z = Math.min(pos1.getZ(), pos2.getZ());
        mz = Math.max(pos1.getZ(), pos2.getZ());
        this.min = new BlockPos(x, y, z);
        this.max = new BlockPos(mx, my, mz);
        this.world = world;
        this.tp = tp;
        this.permissionManager = new PermissionManager(new CompoundTag());
    }

    public AbstractClaim(CompoundTag tag) {
        fromNBT(tag);
    }

    public void fromNBT(CompoundTag tag) {
        this.name = tag.getString("name");
        this.owner = tag.getUuid("owner");
        CompoundTag position = tag.getCompound("position");
        this.min = Util.blockPosFromNBT(position.getCompound("min"));
        this.max = Util.blockPosFromNBT(position.getCompound("max"));
        this.tp = Util.blockPosFromNBT(position.getCompound("tp"));
        //TODO: Add option to ignore claims which are located in unknown worlds
        this.world = WorldUtil.getWorld(position.getString("world"));
        if (tag.contains("subzones")) {
            ListTag list = (ListTag) tag.get("subzones");
            list.forEach(subzones -> {
                Subzone subzone = new Subzone((CompoundTag) subzones, this);
                subzoneList.add(subzone);
            });
        }
        this.permissionManager = new PermissionManager(tag.getCompound("permissions"));
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", this.name);
        tag.putUuid("owner", this.owner);
        CompoundTag position = new CompoundTag();
        position.put("min", Util.blockPosToNBT(this.min));
        position.put("max", Util.blockPosToNBT(this.max));
        if (tp != null) {
            position.put("tp", Util.blockPosToNBT(this.tp));
        }
        position.putString("world", WorldUtil.toIdentifier(this.world));
        tag.put("position", position);
        if (!subzoneList.isEmpty()) {
            ListTag list = new ListTag();
            subzoneList.forEach(subzone -> {
                list.add(subzone.toNBT());
            });
            tag.put("subzones", list);
        }
        tag.put("permissions", this.permissionManager.toNBT());
        return tag;
    }


    public String getName() {
        return this.name;
    }

    public abstract String getFullName();

    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public List<Subzone> getSubzones() {
        return this.subzoneList;
    }

    public void addSubzone(Subzone subzone) {
        this.subzoneList.add(subzone);
    }

    public void removeSubzone(Subzone subzone) {
        this.subzoneList.remove(subzone);
    }

    public boolean hasPermission(UUID uuid, String permission) {
        return this.permissionManager.hasPermission(uuid, permission);
    }

    public PermissionManager getPermissionManager() {
        return this.permissionManager;
    }

    public abstract int getDepth();

    public int getArea() {
        return getSize().getX() * getSize().getZ();
    }

    public BlockPos getSize() {
        return max.subtract(min);
    }

    public boolean contains(BlockPos pos) {
        return (min.getX() <= pos.getX() && max.getX() >= pos.getX()) && (min.getY() <= pos.getY() && max.getY() >= pos.getY()) && (min.getZ() <= pos.getZ() && max.getZ() >= pos.getZ());
    }

    public boolean intersects(AbstractClaim claim) {
        if (!claim.world.equals(this.world)) return false;
        BlockPos a = min, b = max, c = new BlockPos(max.getX(), min.getY(), min.getZ()), d = new BlockPos(min.getX(), max.getY(), min.getZ()), e = new BlockPos(min.getX(), min.getY(), max.getZ()), f = new BlockPos(max.getX(), max.getY(), min.getZ()), g = new BlockPos(max.getX(), min.getY(), max.getZ()), h = new BlockPos(min.getX(), max.getY(), max.getZ());
        return claim.contains(a) || claim.contains(b) || claim.contains(c) || claim.contains(d) || claim.contains(e) || claim.contains(f) || claim.contains(g) || claim.contains(h);
    }

    /**
     * @param uuid      uuid of the player who issued the expansion (this is used to check for claim blocks)
     * @param direction the direction in which a claim should get expanded
     * @param amount    the amount of blocks the claim should get expanded
     * @return amount of claim blocks used
     * @throws CommandSyntaxException if the claim couldn't get expanded
     */
    public abstract int expand(UUID uuid, Direction direction, int amount) throws CommandSyntaxException;

    void undoExpand(Direction direction, int amount) {
        this.expand(direction, -amount);
        for (ServerPlayerEntity player : ItsOursMod.server.getPlayerManager().getPlayerList()) {
            ClaimPlayer claimPlayer = (ClaimPlayer) player;
            if (claimPlayer.getLastShowClaim() == this) {
                this.show(player, true);
            }
        }
    }

    public boolean intersects() {
        for (AbstractClaim value : ItsOursMod.INSTANCE.getClaimList().get()) {
            if (value.getDepth() == this.getDepth() && !this.equals(value) && (this.intersects(value))) {
                return true;
            }
        }
        return false;
    }

    void expand(Direction direction, int amount) {
        if (amount < 0) {
            shrink(direction, -amount);
            return;
        }
        BlockPos modifier = new BlockPos(direction.getOffsetX() * amount, direction.getOffsetY() * amount, direction.getOffsetZ() * amount);
        if (modifier.getX() > 0) {
            max = max.add(modifier.getX(), 0, 0);
        } else {
            min = min.add(modifier.getX(), 0, 0);
        }
        if (modifier.getY() > 0) {
            max = max.add(0, modifier.getY(), 0);
        } else {
            min = min.add(0, modifier.getY(), 0);
        }
        if (modifier.getZ() > 0) {
            max = max.add(0, 0, modifier.getZ());
        } else {
            min = min.add(0, 0, modifier.getZ());
        }
    }

    void shrink(Direction direction, int amount) {
        BlockPos modifier = new BlockPos(direction.getOffsetX() * amount, direction.getOffsetY() * amount, direction.getOffsetZ() * amount);
        if (modifier.getX() < 0) {
            min = min.add(-modifier.getX(), 0, 0);
        } else {
            max = max.add(-modifier.getX(), 0, 0);
        }
        if (modifier.getY() < 0) {
            min = min.add(0, -modifier.getY(), 0);
        } else {
            max = max.add(0, -modifier.getY(), 0);
        }
        if (modifier.getZ() < 0) {
            min = min.add(0, 0, -modifier.getZ());
        } else {
            max = max.add(0, 0, -modifier.getZ());
        }
    }

    public void show(boolean show) {
        for (ServerPlayerEntity player : ItsOursMod.server.getPlayerManager().getPlayerList()) {
            ClaimPlayer claimPlayer = (ClaimPlayer) player;
            if (claimPlayer.getLastShowClaim() == this) {
                this.show(player, show);
            }
        }
    }

    public void show(ServerPlayerEntity player, boolean show) {
        BlockState blockState = show ? showBlocks[Math.min(this.getDepth(), showBlocks.length-1)].getDefaultState() : null;
        int y = ((ClaimPlayer)player).getLastShowPos().getY();
        for (int i = min.getX(); i < max.getX(); i++) {
            sendBlockPacket(player, new BlockPos(getPosOnGround(new BlockPos(i, y, min.getZ()), player.getEntityWorld())).down(), blockState);
        }
        for (int i = min.getZ(); i < max.getZ(); i++) {
            sendBlockPacket(player, new BlockPos(getPosOnGround(new BlockPos(max.getX(), y, i), player.getEntityWorld())).down(), blockState);
        }
        for (int i = max.getX(); i > min.getX(); i--) {
            sendBlockPacket(player, new BlockPos(getPosOnGround(new BlockPos(i, y, max.getZ()), player.getEntityWorld())).down(), blockState);
        }
        for (int i = max.getZ(); i > min.getZ(); i--) {
            sendBlockPacket(player, new BlockPos(getPosOnGround(new BlockPos(min.getX(), y, i), player.getEntityWorld())).down(), blockState);
        }
        for (Subzone subzone : this.getSubzones()) {
            subzone.show(player, show);
        }
    }

    private void sendBlockPacket(ServerPlayerEntity player, BlockPos pos, BlockState state) {
        BlockUpdateS2CPacket packet;
        packet = state == null ? new BlockUpdateS2CPacket(player.getEntityWorld(), pos) : new BlockUpdateS2CPacket(pos, state);
        player.networkHandler.sendPacket(packet);
    }

    public static class Util {
        public static CompoundTag blockPosToNBT(BlockPos pos) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            return tag;
        }

        public static BlockPos blockPosFromNBT(CompoundTag tag) {
            return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        }

        public static BlockPos getPosOnGround(BlockPos pos, World world) {
            BlockPos blockPos = new BlockPos(pos.getX(), pos.getY() + 10, pos.getZ());

            do {
                blockPos = blockPos.down();
                if (blockPos.getY() < 1) {
                    return pos;
                }
            } while (!world.getBlockState(blockPos).isFullCube(world, pos));

            return blockPos.up();
        }
    }

}
