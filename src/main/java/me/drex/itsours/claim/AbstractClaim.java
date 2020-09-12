package me.drex.itsours.claim;

import com.sun.istack.internal.Nullable;
import me.drex.itsours.claim.permission.PermissionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.dimension.DimensionType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public abstract class AbstractClaim {

    private static final Pattern NAME = Pattern.compile("\\w{3,16}");
    private final String name;
    private UUID owner;
    private BlockPos min, max, tppos;
    private final DimensionType dimension;
    private List<Subzone> subzoneList = new ArrayList<>();
    private Date created;
    private Date lastEdited;
    private PermissionManager permissionManager = new PermissionManager();

    public AbstractClaim(String name, UUID owner, BlockPos pos1, BlockPos pos2, DimensionType dimension, @Nullable BlockPos tppos) {
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
        this.dimension = dimension;
        this.tppos = tppos;
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

    public DimensionType getDimension() {
        return this.dimension;
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

}
