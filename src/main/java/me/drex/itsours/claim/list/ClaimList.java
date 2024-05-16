package me.drex.itsours.claim.list;

import com.mojang.serialization.Codec;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.claim.list.quadtree.Quadtree;
import me.drex.itsours.util.ClaimBox;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class ClaimList {

    public static final Codec<List<Claim>> CODEC = Codec.list(Claim.CODEC);

    private static final int MAX_WORLD_SIZE = 29999984;
    public static final Quadtree quadtree = new Quadtree(new BlockPos(-MAX_WORLD_SIZE, -Integer.MAX_VALUE, -MAX_WORLD_SIZE), new BlockPos(MAX_WORLD_SIZE, Integer.MAX_VALUE, MAX_WORLD_SIZE), 16);
    private static final Map<UUID, List<Claim>> byOwner = new HashMap<>();
    private static final List<AbstractClaim> claims = new LinkedList<>();
    private static boolean initialized = false;

    public static void load(List<Claim> claimList) {
        if (initialized) throw new IllegalStateException();
        for (Claim claim : claimList) {
            addClaim(claim);
            addSubzones(claim);
        }
        initialized = true;
    }
    
    private static void addSubzones(AbstractClaim claim) {
        for (Subzone subzone : claim.getSubzones()) {
            addClaim(subzone);
            addSubzones(subzone);
        }
    }

    public static void addClaim(AbstractClaim claim) {
        claims.add(claim);

        if (claim instanceof Claim mainClaim) {
            List<Claim> ownerClaims = byOwner.computeIfAbsent(claim.getOwner(), (ignored) -> new LinkedList<>());
            ownerClaims.add(mainClaim);
        }
        quadtree.insert(claim);
    }

    public static void removeClaim(AbstractClaim claim) {
        claims.remove(claim);

        if (claim instanceof Claim mainClaim) {
            final List<Claim> ownerClaims = byOwner.computeIfAbsent(claim.getOwner(), (ignored) -> new LinkedList<>());
            ownerClaims.remove(mainClaim);
        }

        quadtree.remove(claim);
    }

    public static Optional<AbstractClaim> getClaimAt(Entity entity) {
        return getClaimAt(entity.getWorld(), entity.getBlockPos());
    }

    public static Optional<AbstractClaim> getClaimAt(ItemUsageContext context) {
        return getClaimAt(context.getWorld(), context.getBlockPos());
    }

    public static Optional<AbstractClaim> getClaimAt(World world, BlockPos pos) {
        List<AbstractClaim> claims = quadtree.query(pos);
        AbstractClaim result = null;
        int maxDepth = -1;
        for (AbstractClaim claim : claims) {
            // Check world and y-axis
            if (!claim.getDimension().equals(world.getRegistryKey()) || !claim.contains(pos)) continue;
            // Get claim with the highest claim depth
            if (claim.getDepth() > maxDepth) {
                maxDepth = claim.getDepth();
                result = claim;
            }
        }
        return Optional.ofNullable(result);
    }

    public static List<AbstractClaim> getIntersectingClaims(World world, ClaimBox box) {
        List<AbstractClaim> claims = quadtree.queryIntersections(box);
        claims.removeIf(abstractClaim -> !abstractClaim.getDimension().equals(world.getRegistryKey()));
        return claims;
    }

    public static List<Claim> getClaimsFrom(UUID uuid) {
        return byOwner.getOrDefault(uuid, Collections.emptyList());
    }

    public static List<AbstractClaim> getClaims() {
        return claims;
    }

    private static AbstractClaim getDeepestClaim(AbstractClaim claim, BlockPos pos) {
        for (Subzone subzone : claim.getSubzones()) {
            if (subzone.contains(pos)) return getDeepestClaim(subzone, pos);
        }
        return claim;
    }

    public static Optional<? extends AbstractClaim> getClaim(String name) {
        for (AbstractClaim claim : claims) {
            if (claim.getFullName().equals(name)) return Optional.of(claim);
        }
        return Optional.empty();
    }

}
