package me.drex.itsours.claim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.drex.itsours.claim.flags.holder.FlagData;
import me.drex.itsours.claim.groups.ClaimGroupManager;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.util.ClaimMessages;
import me.drex.itsours.user.ClaimTrackingPlayer;
import me.drex.itsours.util.ClaimBox;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.World;

import java.util.*;

public class Claim extends AbstractClaim {

    public static final Codec<Claim> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(AbstractClaim::getName),
        Uuids.INT_STREAM_CODEC.fieldOf("owner").forGetter(AbstractClaim::getOwner),
        ClaimBox.CODEC.fieldOf("box").forGetter(AbstractClaim::getBox),
        World.CODEC.fieldOf("dimension").forGetter(AbstractClaim::getDimension),
        Codec.list(Subzone.CODEC).fieldOf("subzones").forGetter(AbstractClaim::getSubzones),
        FlagData.CODEC.fieldOf("flags").forGetter(AbstractClaim::getFlags),
        Codec.unboundedMap(Uuids.STRING_CODEC, FlagData.CODEC).fieldOf("player_flags").forGetter(AbstractClaim::getPlayerFlags),
        ClaimGroupManager.CODEC.fieldOf("groups").forGetter(AbstractClaim::getGroupManager),
        ClaimMessages.CODEC.fieldOf("messages").forGetter(AbstractClaim::getMessages)
    ).apply(instance, (name, owner, box, dimension, subzones, flags, playerFlags, groups, claimMessages) -> {
        Claim claim = new Claim(name, owner, box, dimension, subzones, flags, playerFlags, groups, claimMessages);
        subzones.forEach(subzone -> subzone.setParent(claim));
        return claim;
    }));

    private UUID owner;

    public Claim(String name, UUID owner, ClaimBox box, RegistryKey<World> dimension, List<Subzone> subzones, FlagData flags, Map<UUID, FlagData> playerFlags, ClaimGroupManager groups, ClaimMessages messages) {
        super(name, box, dimension, new ArrayList<>(subzones), flags, new HashMap<>(playerFlags), groups, messages);
        this.owner = owner;
    }

    public Claim(String name, UUID owner, ClaimBox box, ServerWorld world) {
        super(name, box, world);
        this.owner = owner;
    }

    @Override
    public boolean canRename(String newName) {
        if (ClaimList.getClaim(newName).isEmpty()) {
            return super.canRename(newName);
        }
        return false;
    }

    @Override
    public String getFullName() {
        return getName();
    }

    @Override
    public Claim getMainClaim() {
        return this;
    }

    @Override
    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(UUID owner) {
        ClaimList.removeClaim(this);
        this.owner = owner;
        ClaimList.addClaim(this);
    }

    @Override
    public int getDepth() {
        return 0;
    }

    public void notifyTrackingChanges(MinecraftServer server) {
        for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
            ClaimTrackingPlayer claimTrackingPlayer = ((ClaimTrackingPlayer) serverPlayerEntity);
            Claim trackedClaim = ((ClaimTrackingPlayer) serverPlayerEntity).trackedClaim();
            if (trackedClaim != null && trackedClaim.equals(this)) {
                claimTrackingPlayer.trackClaim(trackedClaim);
            }
        }
    }

}
