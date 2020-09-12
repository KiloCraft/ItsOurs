package me.drex.itsours.claim;

import com.sun.istack.internal.Nullable;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.util.WorldUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public abstract class AbstractClaim {

    private static final Pattern NAME = Pattern.compile("\\w{3,16}");
    private String name;
    private UUID owner;
    private BlockPos min, max, tp;
    private ServerWorld world;
    private List<Subzone> subzoneList = new ArrayList<>();
    private Date created;
    private Date lastEdited;
    private PermissionManager permissionManager;

    public AbstractClaim(String name, UUID owner, BlockPos pos1, BlockPos pos2, ServerWorld world, @Nullable BlockPos tp) {
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
        this.permissionManager = new PermissionManager();
    }

    public AbstractClaim(CompoundTag tag) {
        fromNBT(tag);
        //TODO: Add claim and subzones to claim list
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
        position.put("tp", Util.blockPosToNBT(this.tp));
        position.putString("world", WorldUtil.toIdentifier(this.world));
        tag.put("position", position);
        if (!subzoneList.isEmpty()) {
            ListTag list = new ListTag();
            subzoneList.forEach(subzone -> {
                list.add(subzone.toNBT());
            });
            tag.put("subzones", list);
        }
        tag.put("permission", this.permissionManager.toNBT());
        return tag;
    }


    public String getName() {
        return this.name;
    }

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

    public abstract void canExpand(Direction direction, int amount, Consumer<ExpandResult> result);

    private void expand(Direction direction, int amount) {
        //TODO: Implement
    }

    public abstract boolean isSubzone();

    public enum ExpandResult {
        MISSING_BLOCKS("You don't have enough blocks.", false),
        OUTSIDE_CLAIM("You can't expand outside of the parent claim.", false),
        COLLISION("You can't expand into another claim.", false),
        TOO_SMALL("You can't shrink your claim smaller than 3 blocks", false),
        SUCCESS();
        private String error = "";
        private boolean success = false;

        ExpandResult(String error, boolean success) {
            this.error = error;
            this.success = success;
        }

        ExpandResult() {
        }

        public boolean success() {
            return this.success;
        }

        public String error() {
            return this.error;
        }
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
    }

}
