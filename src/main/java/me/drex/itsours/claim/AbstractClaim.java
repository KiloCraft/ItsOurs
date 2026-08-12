package me.drex.itsours.claim;

import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.context.*;
import me.drex.itsours.claim.flags.holder.FlagData;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.util.Value;
import me.drex.itsours.claim.flags.visitor.FlagVisitor;
import me.drex.itsours.claim.groups.ClaimGroupManager;
import me.drex.itsours.claim.groups.Group;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.util.ClaimMessages;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.user.ClaimTrackingPlayer;
import me.drex.itsours.util.ClaimBox;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Relative;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

import static me.drex.itsours.util.PlaceholderUtil.*;
import static me.drex.itsours.util.LocalizedMessage.localized;
import static me.drex.itsours.util.LocalizedMessage.sendMessage;
import static net.minecraft.network.chat.Component.literal;

public abstract class AbstractClaim {

    public static final Pattern NAME = Pattern.compile("\\w{3,16}");
    public static final Block[] SHOW_BLOCKS = {Blocks.WOOL.red(), Blocks.WOOL.orange(), Blocks.WOOL.yellow(), Blocks.WOOL.lime(), Blocks.WOOL.green(), Blocks.WOOL.cyan(), Blocks.WOOL.lightBlue(), Blocks.WOOL.blue(), Blocks.WOOL.purple(), Blocks.WOOL.magenta(), Blocks.WOOL.pink()};
    public static final Block[] SHOW_BLOCKS_CENTER = {Blocks.DYED_TERRACOTTA.blue(), Blocks.DYED_TERRACOTTA.purple(), Blocks.DYED_TERRACOTTA.magenta(), Blocks.DYED_TERRACOTTA.pink(), Blocks.DYED_TERRACOTTA.red(), Blocks.DYED_TERRACOTTA.orange(), Blocks.DYED_TERRACOTTA.yellow(), Blocks.DYED_TERRACOTTA.lime(), Blocks.DYED_TERRACOTTA.green(), Blocks.DYED_TERRACOTTA.cyan(), Blocks.DYED_TERRACOTTA.lightBlue()};
    private final ResourceKey<Level> dimension;
    private final List<Subzone> subzones;
    private final FlagData flags;
    private final Map<UUID, FlagData> playerFlags;
    private final ClaimGroupManager groupManager;
    private final ClaimMessages messages;
    private String name;
    private ClaimBox box;


    public AbstractClaim(String name, ClaimBox box, Level world) {
        this(name, box, world.dimension(), new ArrayList<>(), new FlagData(), new HashMap<>(), new ClaimGroupManager(), new ClaimMessages());
    }

    public AbstractClaim(String name, ClaimBox box, ResourceKey<Level> dimension, List<Subzone> subzones, FlagData flags, Map<UUID, FlagData> playerFlags, ClaimGroupManager groupManager, ClaimMessages messages) {
        this.name = name;
        this.box = box;
        this.dimension = dimension;
        this.subzones = subzones;
        this.flags = flags;
        this.playerFlags = playerFlags;
        this.groupManager = groupManager;
        this.messages = messages;
    }

    public static boolean isNameInvalid(String name) {
        return !NAME.matcher(name).matches();
    }

    public static BlockPos getPosOnGround(BlockPos pos, Level world) {
        BlockPos blockPos = new BlockPos(pos.getX(), pos.getY() + 10, pos.getZ());

        do {
            blockPos = blockPos.below();
            if (blockPos.getY() < 1) {
                return pos;
            }
        } while (world.getBlockState(blockPos).getCollisionShape(world, pos).isEmpty());

        return blockPos.above();
    }

    public FlagData getFlags() {
        return flags;
    }

    public Map<UUID, FlagData> getPlayerFlags() {
        return playerFlags;
    }

    public ClaimGroupManager getGroupManager() {
        return groupManager;
    }

    public boolean canRename(String newName) {
        return !isNameInvalid(newName);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClaimBox getBox() {
        return box;
    }

    public void setBox(ClaimBox box) {
        ClaimList.removeClaim(this);
        this.box = box;
        ClaimList.addClaim(this);
    }

    public abstract String getFullName();

    public abstract Claim getMainClaim();

    public abstract UUID getOwner();

    public void setEnterMessage(String enterMessage) {
        this.messages.setEnter(enterMessage);
    }

    public void setLeaveMessage(String leaveMessage) {
        this.messages.setLeave(leaveMessage);
    }

    public ResourceKey<Level> getDimension() {
        return this.dimension;
    }

    public List<Subzone> getSubzones() {
        return this.subzones;
    }

    public void addSubzone(Subzone subzone) {
        this.subzones.add(subzone);
    }

    public void removeSubzone(Subzone subzone) {
        this.subzones.remove(subzone);
    }

    public void onEnter(@Nullable AbstractClaim previousClaim, ServerPlayer player) {
        boolean isAllowed = ItsOurs.checkPermission(player.createCommandSourceStack(), "itsours.fly", 2) &&
            checkAction(player.getUUID(), Flags.CLAIM_FLY);
        updateFly(player, isAllowed);

        sendMessage(player, messages.enter().map(Component::literal).orElse(localized("text.itsours.claim.enter", placeholders(player.level().getServer()), player)), true);
    }

    public void onLeave(@Nullable AbstractClaim nextClaim, ServerPlayer player) {
        if (nextClaim == null) {

            updateFly(player, false);
            sendMessage(player, messages.leave().map(Component::literal).orElse(localized("text.itsours.claim.leave", placeholders(player.level().getServer()), player)), true);
        }
    }

    private static void updateFly(ServerPlayer player, boolean isAllowed) {
        boolean cachedFlying = player.getAbilities().flying;
        boolean cachedAllowFlying = player.getAbilities().mayfly;
        boolean requiresUpdate = false;
        // Update abilities for respective gamemode
        player.gameMode.getGameModeForPlayer().updatePlayerAbilities(player.getAbilities());

        // Enable flying if player enabled it
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = DataManager.getUserData(player.getUUID()).flight() && isAllowed;
        }
        // Set the flight state to what it was before
        if (player.getAbilities().mayfly) {
            player.getAbilities().flying = cachedFlying;
        }

        if (cachedFlying && !player.getAbilities().flying) {
            BlockPos pos = getPosOnGround(player.blockPosition(), player.level());
            player.teleportTo(player.level(), player.getX(), pos.getY(), player.getZ(), EnumSet.noneOf(Relative.class), player.getYRot(), player.getXRot(), true);
            player.level()
                .sendParticles(
                    ParticleTypes.PORTAL,
                    pos.getX(), player.getEyeY(), pos.getZ(),
                    64,
                    0, 0, 0,
                    1
                );
            player.level().playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 2.0F);
        }

        requiresUpdate |= cachedFlying != player.getAbilities().flying;
        requiresUpdate |= cachedAllowFlying != player.getAbilities().mayfly;

        if (requiresUpdate) {
            player.onUpdateAbilities();
        }
    }

    public boolean checkAction(@Nullable UUID uuid, Flag flag) {
        FlagVisitor visitor = FlagVisitor.create();
        visit(uuid, flag, visitor);
        return visitor.getResult().value;
    }

    /**
     * @param uuid  The uuid of the player that should be checked or null if action is not player specific
     * @param nodes A list of nodes that form the flag that shall be checked
     * @return true if the requested action should proceed
     */
    public boolean checkAction(@Nullable UUID uuid, ChildNode... nodes) {
        return checkAction(uuid, Flag.flag(nodes));
    }

    public void visit(@Nullable UUID uuid, Flag flag, FlagVisitor visitor) {
        DataManager.defaultSettings().visit(this, flag, DefaultContext.INSTANCE, visitor);
        flags.visit(this, flag, GlobalContext.INSTANCE, visitor);
        if (uuid != null) {
            if (Objects.equals(uuid, getOwner())) visitor.visit(this, flag, OwnerContext.INSTANCE, Value.ALLOW);
            if (DataManager.getUserData(uuid).ignore())
                visitor.visit(this, flag, IgnoreContext.INSTANCE, Value.ALLOW);
            FlagData playerFlags = this.playerFlags.get(uuid);
            if (playerFlags != null) {
                playerFlags.visit(this, flag, PlayerContext.INSTANCE, visitor);
            }
            for (Map.Entry<String, Group> groupEntry : groupManager.groups().entrySet()) {
                if (groupEntry.getValue().players().contains(uuid)) {
                    groupEntry.getValue().flags().visit(this, flag, new GroupContext(groupEntry.getKey(), groupManager.getPriority(groupEntry.getKey()), groupEntry.getValue()), visitor);
                }
            }
        }
    }

    public boolean containsClaim(AbstractClaim claim) {
        if (claim.equals(this)) return true;
        for (Subzone subzone : claim.getSubzones()) {
            if (containsClaim(subzone)) return true;
        }
        return false;
    }

    public ClaimMessages getMessages() {
        return this.messages;
    }

    public abstract int getDepth();

    public int getArea() {
        return box.getXSpan() * box.getZSpan();
    }

    public Vec3i getSize() {
        return box.getLength().offset(1, 1, 1);
    }

    public boolean contains(BlockPos pos) {
        return box.isInside(pos);
    }

    public void notifyTrackingChanges(MinecraftServer server, boolean add) {
        for (ServerPlayer serverPlayerEntity : server.getPlayerList().getPlayers()) {
            ClaimTrackingPlayer claimTrackingPlayer = ((ClaimTrackingPlayer) serverPlayerEntity);
            claimTrackingPlayer.notifyChange(this, add);
        }
    }

    public String toString() {
        return String.format("%s[name=%s, full_name=%s, owner=%s, box=%s, world=%s, subzones=%s]", this.getClass().getSimpleName(), this.name, this.getFullName(), this.getOwner(), this.box.toString(), this.dimension.toString(), Arrays.toString(subzones.toArray()));
    }

    public Map<String, Component> placeholders(MinecraftServer server, String prefix) {
        return mergePlaceholderMaps(
            Map.of(
                prefix + "full_name", literal(getFullName()),
                prefix + "name", literal(name),
                prefix + "depth", literal(String.valueOf(getDepth())),
                prefix + "dimension", literal(dimension.identifier().toString()),
                prefix + "subzones", Component.literal(String.valueOf(subzones.size())),
                prefix + "trusted", list(groupManager.trusted.players(), uuid -> uuid("trusted_", uuid, server), "text.itsours.placeholders.trusted"),
                prefix + "flags", flags.toText()
            ),
            uuid(prefix + "owner_", getOwner(), server),
            vec3i(prefix + "min_", box.getMin()),
            vec3i(prefix + "max_", box.getMax()),
            vec3i(prefix + "size_", box.getLength().offset(1, 1, 1)),
            vec3i(prefix + "center_", box.getCenter())
        );
    }

    public Map<String, Component> placeholders(MinecraftServer server) {
        return placeholders(server, "claim_");
    }

}
