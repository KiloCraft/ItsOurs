package me.drex.itsours.claim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.holder.PermissionData;
import me.drex.itsours.claim.permission.visitor.PermissionVisitor;
import me.drex.itsours.claim.roles.ClaimRoleManager;
import me.drex.itsours.claim.util.ClaimMessages;
import me.drex.itsours.util.ClaimBox;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Subzone extends AbstractClaim {

    AbstractClaim parent;    public static final Codec<Subzone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(AbstractClaim::getName),
        ClaimBox.CODEC.fieldOf("box").forGetter(AbstractClaim::getBox),
        World.CODEC.fieldOf("dimension").forGetter(AbstractClaim::getDimension),
        Codecs.createLazy(() -> Codec.list(Subzone.CODEC)).optionalFieldOf("subzones", new ArrayList<>()).forGetter(AbstractClaim::getSubzones),
        PermissionData.CODEC.optionalFieldOf("settings", new PermissionData()).forGetter(AbstractClaim::getSettings),
        Codec.unboundedMap(Uuids.STRING_CODEC, PermissionData.CODEC).optionalFieldOf("permissions", new HashMap<>()).forGetter(AbstractClaim::getPermissions),
        ClaimRoleManager.CODEC.optionalFieldOf("roles", new ClaimRoleManager()).forGetter(AbstractClaim::getRoleManager),
        ClaimMessages.CODEC.optionalFieldOf("messages", new ClaimMessages()).forGetter(AbstractClaim::getMessages)
    ).apply(instance, (name, box, dimension, subzones, settings, permissions, roles, claimMessages) -> {
        Subzone claim = new Subzone(name, box, dimension, subzones, settings, permissions, roles, claimMessages);
        subzones.forEach(subzone -> subzone.setParent(claim));
        return claim;
    }));

    private Subzone(String name, ClaimBox box, RegistryKey<World> dimension, List<Subzone> subzones, PermissionData settings, Map<UUID, PermissionData> permissions, ClaimRoleManager roles, ClaimMessages messages) {
        super(name, box, dimension, new ArrayList<>(subzones), settings, new HashMap<>(permissions), roles, messages);
    }

    public Subzone(String name, ClaimBox box, ServerWorld world, AbstractClaim parent) {
        super(name, box, world);
        // Make sure the parent isn't also in the subzone list (getDepth() would get an infinite loop)
        this.parent = parent;
        this.parent.addSubzone(this);
    }

    public AbstractClaim getParent() {
        return this.parent;
    }

    // This method should only be used by codecs
    protected void setParent(AbstractClaim parent) {
        this.parent = parent;
    }

    @Override
    public String getFullName() {
        return parent.getFullName() + "." + getName();
    }

    @Override
    public Claim getMainClaim() {
        return getParent().getMainClaim();
    }

    @Override
    public UUID getOwner() {
        return this.parent.getOwner();
    }

    @Override
    public boolean canRename(String newName) {
        for (Subzone sibling : parent.getSubzones()) {
            if (Objects.equals(sibling.getName(), newName)) {
                return false;
            }
        }
        return super.canRename(newName);
    }

    public int getDepth() {
        return this.parent.getDepth() + 1;
    }

    @Override
    public void visit(@Nullable UUID uuid, Permission permission, PermissionVisitor visitor) {
        this.parent.visit(uuid, permission, visitor);
        super.visit(uuid, permission, visitor);
    }

}
