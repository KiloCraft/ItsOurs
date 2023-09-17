package me.drex.itsours.claim;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.*;

public class ClaimList {

    public static final Codec<List<Claim>> CODEC = Codec.list(Claim.CODEC);

    private static final int FACTOR = 4; // Chunks: 2^4=16

    private static final Long2ObjectMap<List<AbstractClaim>> byPosition = new Long2ObjectLinkedOpenHashMap<>();
    private static final Map<UUID, List<Claim>> byOwner = new HashMap<>();
    private static final List<AbstractClaim> claims = new LinkedList<>();

    public static void load(List<Claim> claimList) {
        // Clear previous data
        byPosition.clear();
        byOwner.clear();
        claims.clear();
        for (Claim claim : claimList) {
            for (Subzone subzone : claim.getSubzones()) {
                addClaim(subzone);
            }
            addClaim(claim);
        }
    }

    public static void addClaim(AbstractClaim claim) {
        claims.add(claim);

        if (claim instanceof Claim mainClaim) {
            List<Claim> ownerClaims = byOwner.computeIfAbsent(claim.getOwner(), (ignored) -> new LinkedList<>());
            ownerClaims.add(mainClaim);
        }
        BlockPos min = claim.getBox().getMin();
        BlockPos max = claim.getBox().getMax();
        for (int x = min.getX() >> FACTOR; x <= max.getX() >> FACTOR; x++) {
            for (int z = min.getZ() >> FACTOR; z <= max.getZ() >> FACTOR; z++) {
                long l = ChunkPos.toLong(x, z);
                List<AbstractClaim> positionClaims = byPosition.computeIfAbsent(l, (ignored) -> new LinkedList<>());
                positionClaims.add(claim);
            }
        }
    }

    public static void removeClaim(AbstractClaim claim) {
        claims.remove(claim);

        if (claim instanceof Claim mainClaim) {
            final List<Claim> ownerClaims = byOwner.computeIfAbsent(claim.getOwner(), (ignored) -> new LinkedList<>());
            ownerClaims.remove(mainClaim);
        }

        BlockPos min = claim.getBox().getMin();
        BlockPos max = claim.getBox().getMax();
        for (int x = min.getX() >> FACTOR; x <= max.getX() >> FACTOR; x++) {
            for (int z = min.getZ() >> FACTOR; z <= max.getZ() >> FACTOR; z++) {
                long l = ChunkPos.toLong(x, z);
                List<AbstractClaim> positionClaims = byPosition.computeIfAbsent(l, (ignored) -> new LinkedList<>());
                positionClaims.remove(claim);
            }
        }
    }

    public static Optional<AbstractClaim> getClaimAt(Entity entity) {
        return getClaimAt((ServerWorld) entity.getWorld(), entity.getBlockPos());
    }

    public static Optional<AbstractClaim> getClaimAt(ItemUsageContext context) {
        return getClaimAt((ServerWorld) context.getWorld(), context.getBlockPos());
    }

    public static Optional<AbstractClaim> getClaimAt(ServerWorld serverWorld, BlockPos pos) {
        List<AbstractClaim> claims = byPosition.getOrDefault(ChunkPos.toLong(pos), Collections.emptyList());
        for (AbstractClaim claim : claims) {
            if (!claim.getDimension().equals(serverWorld.getRegistryKey())) continue;
            if (claim.contains(pos)) return Optional.of(getDeepestClaim(claim, pos));
        }
        return Optional.empty();
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
