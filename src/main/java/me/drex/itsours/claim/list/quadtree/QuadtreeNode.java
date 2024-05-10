package me.drex.itsours.claim.list.quadtree;

import com.google.common.base.MoreObjects;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.util.ClaimBox;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class QuadtreeNode {

    ClaimBox boundary;
    List<AbstractClaim> claims;
    QuadtreeNode[] children;

    public QuadtreeNode(ClaimBox boundary) {
        this.boundary = boundary;
        this.claims = new ArrayList<>();
        this.children = new QuadtreeNode[4];
    }

    public boolean isLeaf() {
        return children[0] == null;
    }

    public boolean contains(ClaimBox boundary) {
        return this.boundary.contains(boundary);
    }

    public boolean contains(BlockPos pos) {
        return boundary.contains(pos);
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper builder = MoreObjects.toStringHelper(this).add("boundary", boundary).add("claims", claims.stream().map(AbstractClaim::getName).toList());
        if (!isLeaf()) {
            builder.add("children", children);
        }
        return builder.toString();
    }
}
