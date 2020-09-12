package me.drex.itsours.claim.list;

import me.drex.itsours.claim.AbstractClaim;

import java.util.*;

public class ClaimList<T extends AbstractClaim> {

    private List<T> claimList = new ArrayList<>();
    private Map<UUID, List<T>> byOwner = new HashMap<>();
    private Map<Region, List<T>> byRegion = new HashMap<>();

    public void add(T claim) {
        claimList.add(claim);

        //Map a list of claims to their owner
        UUID owner = claim.getOwner();
        List<T> claims = byOwner.get(owner);
        if (claims == null) {
            claims = new ArrayList<>(Collections.singletonList(claim));
        } else {
            claims.add(claim);
        }
        byOwner.put(claim.getOwner(), claims);
    }

    public List<T> get(UUID owner) {
        return byOwner.get(owner) == null ? new ArrayList<>() : byOwner.get(owner);
    }

}
