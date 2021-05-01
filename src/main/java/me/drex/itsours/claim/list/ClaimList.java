package me.drex.itsours.claim.list;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.Subzone;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

public class ClaimList {

    private final List<AbstractClaim> claimList = new ArrayList<>();
    private final Map<UUID, List<AbstractClaim>> byOwner = new HashMap<>();
    /*
     * byRegion is a HashMap for quick retrieval of claims by a position
     * */
    private final Map<Region, List<AbstractClaim>> byRegion = new HashMap<>();

    public ClaimList(NbtList tag) {
        fromNBT(tag);
    }

    public void fromNBT(NbtList tag) {
        tag.forEach(claimTag -> {
            Claim claim = new Claim((NbtCompound) claimTag);
            this.add(claim);
            for (Subzone subzone : claim.getSubzones()) {
                this.add(subzone);
            }
        });
    }

    public NbtList toNBT() {
        NbtList list = new NbtList();
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
        if (claim instanceof Claim) {
            this.addRegion(Region.get(claim.min.getX(), claim.min.getZ()), claim);
            this.addRegion(Region.get(claim.min.getX(), claim.max.getZ()), claim);
            this.addRegion(Region.get(claim.max.getX(), claim.min.getZ()), claim);
            this.addRegion(Region.get(claim.max.getX(), claim.max.getZ()), claim);
        }
    }

    public void update() {
        byOwner.clear();
        byRegion.clear();
        ArrayList<AbstractClaim> tempList = new ArrayList<>(claimList);
        claimList.clear();
        for (AbstractClaim claim : tempList) {
            this.add(claim);
        }
    }

    public void remove(AbstractClaim claim) {
        claimList.remove(claim);
        update();
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

    public Optional<AbstractClaim> get(ServerWorld world, BlockPos pos) {
        List<AbstractClaim> claims = get(Region.get(pos.getX(), pos.getZ())).stream().filter(abstractClaim -> abstractClaim.getWorld().equals(world)).collect(Collectors.toList());
        for (AbstractClaim claim : claims) {
            if (claim.contains(pos)) {
                return Optional.of(getDeepestClaim(claim, pos));
            }
        }
        return Optional.empty();
    }

    private AbstractClaim getDeepestClaim(AbstractClaim claim, BlockPos pos) {
        for (Subzone subzone : claim.getSubzones()) {
            if (subzone.contains(pos)) return getDeepestClaim(subzone, pos);
        }
        return claim;
    }

    public Optional<Claim> getMainClaim(String name) {
        for (AbstractClaim claim : claimList.stream().filter(claim -> claim instanceof Claim).collect(Collectors.toList())) {
            if (claim.getFullName().equals(name)) return Optional.of((Claim) claim);
        }
        return Optional.empty();
    }

    public Optional<? extends AbstractClaim> get(String name) {
        if (name.contains(".")) {
            String[] names = name.split("\\.");
            for (AbstractClaim claim : ItsOursMod.INSTANCE.getClaimList().get()) {
                if (claim.getName().equals(names[0])) {
                    Optional<Subzone> subzone = getSubzone(claim, name);
                    if (subzone.isPresent()) return subzone;
                }
            }
        } else {
            for (AbstractClaim claim : ItsOursMod.INSTANCE.getClaimList().get().stream().filter(claim -> claim instanceof Claim).collect(Collectors.toList())) {
                if (claim.getName().equals(name)) return Optional.of(claim);
            }
        }
        return Optional.empty();
    }

    private Optional<Subzone> getSubzone(AbstractClaim claim, String name) {
        String[] names = name.split("\\.");
        for (Subzone subzone : claim.getSubzones()) {
            if (subzone.getDepth() > names.length) {
                return Optional.empty();
            }
            if (subzone.getName().equals(names[subzone.getDepth()])) {
                if (subzone.getDepth() == names.length - 1) {
                    return Optional.of(subzone);
                } else {
                    return getSubzone(subzone, name);
                }
            }
        }
        return Optional.empty();
    }

    public boolean contains(String name) {
        return getMainClaim(name).isPresent();
    }

    private List<AbstractClaim> get(Region region) {
        return byRegion.get(region) == null ? new ArrayList<>() : byRegion.get(region);
    }

}
