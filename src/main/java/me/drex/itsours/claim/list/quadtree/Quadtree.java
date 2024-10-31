package me.drex.itsours.claim.list.quadtree;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.util.ClaimBox;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Quadtree {
    private final QuadtreeNode root;
    private final int maxClaimsPerNode; // Max number of claims per node

    public Quadtree(BlockPos minPoint, BlockPos maxPoint, int maxClaimsPerNode) {
        ClaimBox boundary = ClaimBox.create(minPoint, maxPoint);
        this.root = new QuadtreeNode(boundary);
        this.maxClaimsPerNode = maxClaimsPerNode;
    }

    public boolean insert(AbstractClaim claim) {
        return insert(root, claim);
    }

    private boolean insert(QuadtreeNode node, AbstractClaim claim) {
        ClaimBox boundary = claim.getBox();
        if (!node.contains(boundary.getMin()) && !node.contains(boundary.getMax())) {
            return false; // Claim is outside of this node's boundary
        }

        if (node.isLeaf()) {
            if (node.claims.size() < maxClaimsPerNode) {
                node.claims.add(claim);
                return true;
            } else {
                splitNode(node);
            }
        }
        for (QuadtreeNode child : node.children) {
            if (child.contains(boundary)) {
                if (insert(child, claim)) {
                    return true;
                }
            }
        }
        // Claim doesn't fit into any child nodes
        node.claims.add(claim);
        return true;
    }

    public boolean remove(AbstractClaim claim) {
        return remove(root, claim);
    }

    private boolean remove(QuadtreeNode node, AbstractClaim claim) {
        ClaimBox bounding = claim.getBox();
        if (!node.contains(bounding.getMin()) && !node.contains(bounding.getMax())) {
            return false; // Claim is outside of this node's boundary
        }

        if (node.isLeaf()) {
            return node.claims.remove(claim); // Remove claim if it exists in this node
        } else {
            for (QuadtreeNode child : node.children) {
                if (child.contains(bounding)) {
                    return remove(child, claim);
                }
            }

            // The claim spans multiple children
            return node.claims.remove(claim);
        }
    }

    private void splitNode(QuadtreeNode node) {
        BlockPos min = node.boundary.getMin();
        BlockPos max = node.boundary.getMax();
        int midX = (min.getX() + max.getX()) / 2;
        int midZ = (min.getZ() + max.getZ()) / 2;

        BlockPos[] mins = {min, new BlockPos(midX, min.getY(), min.getZ()), new BlockPos(midX, min.getY(), midZ), new BlockPos(min.getX(), min.getY(), midZ)};
        BlockPos[] maxs = {new BlockPos(midX, max.getY(), midZ), new BlockPos(max.getX(), max.getY(), midZ), max, new BlockPos(midX, max.getY(), max.getZ())};

        for (int i = 0; i < 4; i++) {
            ClaimBox childBoundary = ClaimBox.create(mins[i], maxs[i]);
            node.children[i] = new QuadtreeNode(childBoundary);
        }

        // Move existing claims to appropriate children
        LinkedList<AbstractClaim> splitClaims = new LinkedList<>(node.claims);
        node.claims.clear();
        for (AbstractClaim claim : splitClaims) {
            insert(node, claim);
        }
    }

    public List<AbstractClaim> query(BlockPos pos) {
        return query(root, pos);
    }

    private List<AbstractClaim> query(QuadtreeNode node, BlockPos pos) {
        List<AbstractClaim> result = new ArrayList<>();
        if (node.contains(pos)) {
            for (AbstractClaim claim : node.claims) {
                if (claim.getBox().contains(pos)) {
                    result.add(claim);
                }
            }
            if (!node.isLeaf()) {
                for (QuadtreeNode child : node.children) {
                    result.addAll(query(child, pos));
                }
            }
        }
        return result;
    }

    public List<AbstractClaim> queryIntersections(ClaimBox box) {
        return queryIntersections(root, box);
    }

    private List<AbstractClaim> queryIntersections(QuadtreeNode node, ClaimBox box) {
        List<AbstractClaim> result = new ArrayList<>();
        for (AbstractClaim claim : node.claims) {
            if (claim.getBox().intersects(box)) {
                result.add(claim);
            }
        }
        if (node.boundary.intersects(box)) {
            if (!node.isLeaf()) {
                for (QuadtreeNode child : node.children) {
                    result.addAll(queryIntersections(child, box));
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return root.toString();
    }
}
