package me.drex.itsours.claim;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClaimList {

    public static final ClaimList INSTANCE = new ClaimList();
    private static final int FACTOR = 4; // Chunks: 2^4=16

    private final Long2ObjectMap<List<AbstractClaim>> byPosition = new Long2ObjectLinkedOpenHashMap<>();
    private final Map<UUID, List<AbstractClaim>> byOwner = new HashMap<>();
    private final List<AbstractClaim> claims = new LinkedList<>();

    private ClaimList() {
    }

    public void load(@Nullable NbtList tag) {
        // Clear previous data
        byPosition.clear();
        byOwner.clear();
        claims.clear();
        if (tag == null) return;
        for (NbtElement element : tag) {
            Claim claim = new Claim((NbtCompound) element);
            for (Subzone subzone : claim.getSubzones()) {
                this.addClaim(subzone);
            }
            this.addClaim(claim);
        }
    }

    public NbtList save() {
        NbtList list = new NbtList();
        for (AbstractClaim claim : claims) {
            // Main claims handle serialization of subzones
            if (claim instanceof Claim) {
                list.add(claim.toNBT());
            }
        }
        return list;
    }

    public void addClaim(AbstractClaim claim) {
        claims.add(claim);

        final List<AbstractClaim> ownerClaims = byOwner.getOrDefault(claim.getOwner(), new LinkedList<>());
        ownerClaims.add(claim);
        byOwner.put(claim.getOwner(), ownerClaims);

        BlockPos min = claim.getBox().getMin();
        BlockPos max  = claim.getBox().getMax();
        for (int x = min.getX() >> FACTOR; x <= max.getX() >> FACTOR; x++) {
            for (int z = min.getZ() >> FACTOR; z <= max.getZ() >> FACTOR; z++) {
                long l = ChunkPos.toLong(x, z);
                List<AbstractClaim> positionClaims = byPosition.getOrDefault(l, new LinkedList<>());
                positionClaims.add(claim);
                byPosition.put(l, positionClaims);
            }
        }
    }

    public void removeClaim(AbstractClaim claim) {
        claims.remove(claim);

        final List<AbstractClaim> ownerClaims = byOwner.getOrDefault(claim.getOwner(), new LinkedList<>());
        ownerClaims.remove(claim);
        byOwner.put(claim.getOwner(), ownerClaims);

        BlockPos min = claim.getBox().getMin();
        BlockPos max  = claim.getBox().getMax();
        for (int x = min.getX() >> FACTOR; x <= max.getX() >> FACTOR; x++) {
            for (int z = min.getZ() >> FACTOR; z <= max.getZ() >> FACTOR; z++) {
                long l = ChunkPos.toLong(x, z);
                List<AbstractClaim> positionClaims = byPosition.getOrDefault(l, new LinkedList<>());
                positionClaims.remove(claim);
                byPosition.put(l, positionClaims);
            }
        }
    }

    public Optional<AbstractClaim> getClaimAt(Entity entity) {
        return getClaimAt((ServerWorld) entity.getWorld(), entity.getBlockPos());
    }

    public Optional<AbstractClaim> getClaimAt(ItemUsageContext context) {
        return getClaimAt((ServerWorld) context.getWorld(), context.getBlockPos());
    }

    public Optional<AbstractClaim> getClaimAt(ServerWorld serverWorld, BlockPos pos) {
        List<AbstractClaim> claims = byPosition.getOrDefault(ChunkPos.toLong(pos), new LinkedList<>());
        for (AbstractClaim claim : claims) {
            if (!claim.getDimension().equals(serverWorld.getRegistryKey())) continue;
            if (claim.contains(pos)) return Optional.of(getDeepestClaim(claim, pos));
        }
        return Optional.empty();
    }

    public List<AbstractClaim> getClaimsFrom(UUID uuid) {
        return byOwner.getOrDefault(uuid, new LinkedList<>());
    }

    public List<AbstractClaim> getClaims() {
        return claims;
    }

    public Map<UUID, List<AbstractClaim>> getByOwner() {
        return byOwner;
    }

    private AbstractClaim getDeepestClaim(AbstractClaim claim, BlockPos pos) {
        for (Subzone subzone : claim.getSubzones()) {
            if (subzone.contains(pos)) return getDeepestClaim(subzone, pos);
        }
        return claim;
    }

    public Optional<? extends AbstractClaim> getClaim(String name) {
        for (AbstractClaim claim : claims) {
            if (claim.getFullName().equals(name)) return Optional.of(claim);
        }
        return Optional.empty();
    }

}
