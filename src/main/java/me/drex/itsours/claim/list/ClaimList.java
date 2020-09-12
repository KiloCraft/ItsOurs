package me.drex.itsours.claim.list;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.Subzone;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.*;

public class ClaimList {

    private List<AbstractClaim> claimList = new ArrayList<>();
    private Map<UUID, List<AbstractClaim>> byOwner = new HashMap<>();
    private Map<Region, List<AbstractClaim>> byRegion = new HashMap<>();

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

    public List<AbstractClaim> get(UUID owner) {
        return byOwner.get(owner) == null ? new ArrayList<>() : byOwner.get(owner);
    }

}
