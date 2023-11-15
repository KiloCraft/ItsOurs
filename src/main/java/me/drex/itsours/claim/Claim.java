package me.drex.itsours.claim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.drex.itsours.claim.permission.holder.PermissionData;
import me.drex.itsours.claim.roles.ClaimRoleManager;
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
        PermissionData.CODEC.fieldOf("settings").forGetter(AbstractClaim::getSettings),
        Codec.unboundedMap(Uuids.STRING_CODEC, PermissionData.CODEC).fieldOf("permissions").forGetter(AbstractClaim::getPermissions),
        ClaimRoleManager.CODEC.fieldOf("roles").forGetter(AbstractClaim::getRoleManager),
        ClaimMessages.CODEC.fieldOf("messages").forGetter(AbstractClaim::getMessages)
    ).apply(instance, (name, owner, box, dimension, subzones, settings, permissions, roles, claimMessages) -> {
        Claim claim = new Claim(name, owner, box, dimension, subzones, settings, permissions, roles, claimMessages);
        subzones.forEach(subzone -> subzone.setParent(claim));
        return claim;
    }));

    private UUID owner;


    public Claim(String name, UUID owner, ClaimBox box, RegistryKey<World> dimension, List<Subzone> subzones, PermissionData settings, Map<UUID, PermissionData> permissions, ClaimRoleManager roles, ClaimMessages messages) {
        super(name, box, dimension, new ArrayList<>(subzones), settings, new HashMap<>(permissions), roles, messages);
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
