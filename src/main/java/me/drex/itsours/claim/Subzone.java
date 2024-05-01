package me.drex.itsours.claim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.holder.FlagData;
import me.drex.itsours.claim.flags.visitor.FlagVisitor;
import me.drex.itsours.claim.groups.ClaimGroupManager;
import me.drex.itsours.claim.util.ClaimMessages;
import me.drex.itsours.util.ClaimBox;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Subzone extends AbstractClaim {

    public static final Codec<Subzone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(AbstractClaim::getName),
        ClaimBox.CODEC.fieldOf("box").forGetter(AbstractClaim::getBox),
        World.CODEC.fieldOf("dimension").forGetter(AbstractClaim::getDimension),
        Codec.lazyInitialized(() -> Codec.list(Subzone.CODEC)).optionalFieldOf("subzones", new ArrayList<>()).forGetter(AbstractClaim::getSubzones),
        FlagData.CODEC.fieldOf("flags").forGetter(AbstractClaim::getFlags),
        Codec.unboundedMap(Uuids.STRING_CODEC, FlagData.CODEC).optionalFieldOf("player_flags", new HashMap<>()).forGetter(AbstractClaim::getPlayerFlags),
        ClaimGroupManager.CODEC.fieldOf("groups").forGetter(AbstractClaim::getGroupManager),
        ClaimMessages.CODEC.fieldOf("messages").forGetter(AbstractClaim::getMessages)
    ).apply(instance, (name, box, dimension, subzones, flags, playerFlags, groups, claimMessages) -> {
        Subzone claim = new Subzone(name, box, dimension, subzones, flags, playerFlags, groups, claimMessages);
        subzones.forEach(subzone -> subzone.setParent(claim));
        return claim;
    }));

    AbstractClaim parent;

    private Subzone(String name, ClaimBox box, RegistryKey<World> dimension, List<Subzone> subzones, FlagData flags, Map<UUID, FlagData> playerFlags, ClaimGroupManager groups, ClaimMessages messages) {
        super(name, box, dimension, new ArrayList<>(subzones), flags, new HashMap<>(playerFlags), groups, messages);
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
    public void visit(@Nullable UUID uuid, Flag flag, FlagVisitor visitor) {
        this.parent.visit(uuid, flag, visitor);
        super.visit(uuid, flag, visitor);
    }

}
