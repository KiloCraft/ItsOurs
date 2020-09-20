package me.drex.itsours.claim.list;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.Subzone;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

public class ClaimList {

    private List<AbstractClaim> claimList = new ArrayList<>();
    private Map<UUID, List<AbstractClaim>> byOwner = new HashMap<>();
    /*
     * byRegion is a HashMap for quick retrieval of claims by a position
     * */
    private Map<Region, List<AbstractClaim>> byRegion = new HashMap<>();

    public ClaimList(ListTag tag) {
        fromNBT(tag);
    }

    public void fromNBT(ListTag tag) {
        tag.forEach(claimTag -> {
            Claim claim = new Claim((CompoundTag) claimTag);
            this.add(claim);
            for (Subzone subzone : claim.getSubzones()) {
                this.add(subzone);
            }
        });
    }

    public ListTag toNBT() {
        ListTag list = new ListTag();
        for (AbstractClaim claim : claimList) {
            //Filter main claims, since they handle subzone deserialization
            if (claim instanceof Claim) {
                list.add(claim.toNBT());
            }
        }
        return list;
    }

    public void add(AbstractClaim claim) {
        claimList.add(claim);

        //Map a list of claims to their owner
        UUID owner = claim.getOwner();
        List<AbstractClaim> claims = byOwner.get(owner);
        if (claims == null) {
            claims = new ArrayList<>(Collections.singletonList(claim));
        } else {
            claims.add(claim);
        }
        byOwner.put(claim.getOwner(), claims);

        //Map a list of claims to their region
        this.addRegion(Region.get(claim.min.getX(), claim.min.getZ()), claim);
        this.addRegion(Region.get(claim.min.getX(), claim.max.getZ()), claim);
        this.addRegion(Region.get(claim.max.getX(), claim.min.getZ()), claim);
        this.addRegion(Region.get(claim.max.getX(), claim.max.getZ()), claim);
    }

    public void update() {
        byOwner.clear();
        byRegion.clear();
        for (AbstractClaim claim : claimList) {
            this.add(claim);
        }
    }

    private void addRegion(Region region, AbstractClaim claim) {
        List<AbstractClaim> claims = byRegion.get(region);
        claims = claims == null ? new ArrayList<>() : claims;
        claims.add(claim);
        byRegion.put(region, claims);
    }

    public List<AbstractClaim> get() {
        return this.claimList;
    }

    public List<AbstractClaim> get(UUID owner) {
        return byOwner.get(owner) == null ? new ArrayList<>() : byOwner.get(owner);
    }

    public AbstractClaim get(ServerWorld world, BlockPos pos) {
        List<AbstractClaim> claims = get(Region.get(pos.getX(), pos.getZ())).stream().filter(abstractClaim -> abstractClaim.getWorld().equals(world)).collect(Collectors.toList());
        for (AbstractClaim claim : claims) {
            if (claim.contains(pos)) {
                for (Subzone subzone : claim.getSubzones()) {
                    if (subzone.contains(pos)) return subzone;
                }
                return claim;
            }
        }
        return null;
    }

    private List<AbstractClaim> get(Region region) {
        return byRegion.get(region) == null ? new ArrayList<>() : byRegion.get(region);
    }

}
