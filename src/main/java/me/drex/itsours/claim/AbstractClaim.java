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
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

import static me.drex.itsours.util.PlaceholderUtil.*;
import static me.drex.message.api.LocalizedMessage.localized;
import static net.minecraft.text.Text.literal;

public abstract class AbstractClaim {

    public static final Pattern NAME = Pattern.compile("\\w{3,16}");
    public static final Block[] SHOW_BLOCKS = {Blocks.RED_WOOL, Blocks.ORANGE_WOOL, Blocks.YELLOW_WOOL, Blocks.LIME_WOOL, Blocks.GREEN_WOOL, Blocks.CYAN_WOOL, Blocks.LIGHT_BLUE_WOOL, Blocks.BLUE_WOOL, Blocks.PURPLE_WOOL, Blocks.MAGENTA_WOOL, Blocks.PINK_WOOL};
    public static final Block[] SHOW_BLOCKS_CENTER = {Blocks.BLUE_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA};
    private final RegistryKey<World> dimension;
    private final List<Subzone> subzones;
    private final FlagData flags;
    private final Map<UUID, FlagData> playerFlags;
    private final ClaimGroupManager groupManager;
    private final ClaimMessages messages;
    private String name;
    private ClaimBox box;


    public AbstractClaim(String name, ClaimBox box, World world) {
        this(name, box, world.getRegistryKey(), new ArrayList<>(), new FlagData(), new HashMap<>(), new ClaimGroupManager(), new ClaimMessages());
    }

    public AbstractClaim(String name, ClaimBox box, RegistryKey<World> dimension, List<Subzone> subzones, FlagData flags, Map<UUID, FlagData> playerFlags, ClaimGroupManager groupManager, ClaimMessages messages) {
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

    public static BlockPos getPosOnGround(BlockPos pos, World world) {
        BlockPos blockPos = new BlockPos(pos.getX(), pos.getY() + 10, pos.getZ());

        do {
            blockPos = blockPos.down();
            if (blockPos.getY() < 1) {
                return pos;
            }
        } while (world.getBlockState(blockPos).getCollisionShape(world, pos).isEmpty());

        return blockPos.up();
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

    public RegistryKey<World> getDimension() {
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

    public void onEnter(@Nullable AbstractClaim previousClaim, ServerPlayerEntity player) {
        boolean isAllowed = ItsOurs.checkPermission(player.getCommandSource(), "itsours.fly", 2) &&
            checkAction(player.getUuid(), Flags.CLAIM_FLY);
        updateFly(player, isAllowed);

        player.sendMessage(messages.enter().map(Text::literal).orElse(localized("text.itsours.claim.enter", placeholders(player.getEntityWorld().getServer()))), true);
    }

    public void onLeave(@Nullable AbstractClaim nextClaim, ServerPlayerEntity player) {
        if (nextClaim == null) {

            updateFly(player, false);
            player.sendMessage(messages.leave().map(Text::literal).orElse(localized("text.itsours.claim.leave", placeholders(player.getEntityWorld().getServer()))), true);
        }
    }

    private static void updateFly(ServerPlayerEntity player, boolean isAllowed) {
        boolean cachedFlying = player.getAbilities().flying;
        boolean cachedAllowFlying = player.getAbilities().allowFlying;
        boolean requiresUpdate = false;
        // Update abilities for respective gamemode
        player.interactionManager.getGameMode().setAbilities(player.getAbilities());

        // Enable flying if player enabled it
        if (!player.getAbilities().allowFlying) {
            player.getAbilities().allowFlying = DataManager.getUserData(player.getUuid()).flight() && isAllowed;
        }
        // Set the flight state to what it was before
        if (player.getAbilities().allowFlying) {
            player.getAbilities().flying = cachedFlying;
        }

        if (cachedFlying && !player.getAbilities().flying) {
            BlockPos pos = getPosOnGround(player.getBlockPos(), player.getEntityWorld());
            player.teleport(player.getEntityWorld(), player.getX(), pos.getY(), player.getZ(), EnumSet.noneOf(PositionFlag.class), player.getYaw(), player.getPitch(), true);
            player.getEntityWorld()
                .spawnParticles(
                    ParticleTypes.PORTAL,
                    pos.getX(), player.getEyeY(), pos.getZ(),
                    64,
                    0, 0, 0,
                    1
                );
            player.getEntityWorld().playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 2.0F);
        }

        requiresUpdate |= cachedFlying != player.getAbilities().flying;
        requiresUpdate |= cachedAllowFlying != player.getAbilities().allowFlying;

        if (requiresUpdate) {
            player.sendAbilitiesUpdate();
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
        return box.getBlockCountX() * box.getBlockCountZ();
    }

    public Vec3i getSize() {
        return box.getDimensions().add(1, 1, 1);
    }

    public boolean contains(BlockPos pos) {
        return box.contains(pos);
    }

    public void notifyTrackingChanges(MinecraftServer server, boolean add) {
        for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
            ClaimTrackingPlayer claimTrackingPlayer = ((ClaimTrackingPlayer) serverPlayerEntity);
            claimTrackingPlayer.notifyChange(this, add);
        }
    }

    public String toString() {
        return String.format("%s[name=%s, full_name=%s, owner=%s, box=%s, world=%s, subzones=%s]", this.getClass().getSimpleName(), this.name, this.getFullName(), this.getOwner(), this.box.toString(), this.dimension.toString(), Arrays.toString(subzones.toArray()));
    }

    public Map<String, Text> placeholders(MinecraftServer server, String prefix) {
        return mergePlaceholderMaps(
            Map.of(
                prefix + "full_name", literal(getFullName()),
                prefix + "name", literal(name),
                prefix + "depth", literal(String.valueOf(getDepth())),
                prefix + "dimension", literal(dimension.getValue().toString()),
                prefix + "subzones", Text.literal(String.valueOf(subzones.size())),
                prefix + "trusted", list(groupManager.trusted.players(), uuid -> uuid("trusted_", uuid, server), "text.itsours.placeholders.trusted"),
                prefix + "flags", flags.toText()
            ),
            uuid(prefix + "owner_", getOwner(), server),
            vec3i(prefix + "min_", box.getMin()),
            vec3i(prefix + "max_", box.getMax()),
            vec3i(prefix + "size_", box.getDimensions().add(1, 1, 1)),
            vec3i(prefix + "center_", box.getCenter())
        );
    }

    public Map<String, Text> placeholders(MinecraftServer server) {
        return placeholders(server, "claim_");
    }

}
