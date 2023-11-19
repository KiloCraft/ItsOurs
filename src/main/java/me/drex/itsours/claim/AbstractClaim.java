package me.drex.itsours.claim;

import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.context.*;
import me.drex.itsours.claim.permission.holder.PermissionData;
import me.drex.itsours.claim.permission.node.ChildNode;
import me.drex.itsours.claim.permission.util.Value;
import me.drex.itsours.claim.permission.visitor.PermissionVisitor;
import me.drex.itsours.claim.roles.ClaimRoleManager;
import me.drex.itsours.claim.roles.Role;
import me.drex.itsours.claim.util.ClaimMessages;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.util.ClaimBox;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
    private final PermissionData settings;
    private final Map<UUID, PermissionData> permissions;
    private final ClaimRoleManager roleManager;
    private final ClaimMessages messages;
    private String name;
    private ClaimBox box;


    public AbstractClaim(String name, ClaimBox box, ServerWorld world) {
        this(name, box, world.getRegistryKey(), new ArrayList<>(), new PermissionData(), new HashMap<>(), new ClaimRoleManager(), new ClaimMessages());
    }

    public AbstractClaim(String name, ClaimBox box, RegistryKey<World> dimension, List<Subzone> subzones, PermissionData settings, Map<UUID, PermissionData> permissions, ClaimRoleManager roleManager, ClaimMessages messages) {
        this.name = name;
        this.box = box;
        this.dimension = dimension;
        this.subzones = subzones;
        this.settings = settings;
        this.permissions = permissions;
        this.roleManager = roleManager;
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
        } while (!world.getBlockState(blockPos).isFullCube(world, pos));

        return blockPos.up();
    }

    public PermissionData getSettings() {
        return settings;
    }

    public Map<UUID, PermissionData> getPermissions() {
        return permissions;
    }

    public ClaimRoleManager getRoleManager() {
        return roleManager;
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
        boolean hasPermission = ItsOurs.checkPermission(player.getCommandSource(), "itsours.fly", 2) && player.getWorld().getRegistryKey().equals(World.OVERWORLD);
        boolean cachedFlying = hasPermission && player.getAbilities().flying;
        // Update abilities for respective gamemode
        player.interactionManager.getGameMode().setAbilities(player.getAbilities());
        // Enable flying if player enabled it
        if (!player.getAbilities().allowFlying) {
            player.getAbilities().allowFlying = DataManager.getUserData(player.getUuid()).flight() && hasPermission;
        }
        // Set the flight state to what it was before entering
        if (player.getAbilities().allowFlying) {
            player.getAbilities().flying = cachedFlying;
        }
        player.sendAbilitiesUpdate();

        player.sendMessage(messages.enter().map(Text::literal).orElse(localized("text.itsours.claim.enter", placeholders(player.server))), true);
    }

    public void onLeave(@Nullable AbstractClaim nextClaim, ServerPlayerEntity player) {
        if (nextClaim == null) {
            boolean cachedFlying = player.getAbilities().flying;
            // Update abilities for respective gamemode
            player.interactionManager.getGameMode().setAbilities(player.getAbilities());
            if (cachedFlying && !player.getAbilities().flying) {
                BlockPos pos = getPosOnGround(player.getBlockPos(), player.getWorld());
                if (pos.getY() + 3 < player.getY()) {
                    player.teleport(player.getServerWorld(), player.getX(), pos.getY(), player.getZ(), player.getYaw(), player.getPitch());
                }
            }
            player.sendAbilitiesUpdate();
            player.sendMessage(messages.leave().map(Text::literal).orElse(localized("text.itsours.claim.leave", placeholders(player.server))), true);
        }
    }

    public boolean hasPermission(@Nullable UUID uuid, Permission permission) {
        PermissionVisitor visitor = PermissionVisitor.create();
        visit(uuid, permission, visitor);
        return visitor.getResult().value;
    }

    /**
     * @param uuid  The uuid of the player that should be checked or null if action is not player specific
     * @param nodes A list of nodes that form the permission that shall be checked
     * @return true if the requested action should proceed
     */
    public boolean hasPermission(@Nullable UUID uuid, ChildNode... nodes) {
        return hasPermission(uuid, Permission.permission(nodes));
    }

    public void visit(@Nullable UUID uuid, Permission permission, PermissionVisitor visitor) {
        DataManager.defaultSettings().visit(this, permission, DefaultContext.INSTANCE, visitor);
        settings.visit(this, permission, GlobalContext.INSTANCE, visitor);
        if (uuid != null) {
            if (Objects.equals(uuid, getOwner())) visitor.visit(this, permission, OwnerContext.INSTANCE, Value.ALLOW);
            if (DataManager.getUserData(uuid).ignore())
                visitor.visit(this, permission, IgnoreContext.INSTANCE, Value.ALLOW);
            PermissionData playerPermissions = permissions.get(uuid);
            if (playerPermissions != null) {
                playerPermissions.visit(this, permission, PersonalContext.INSTANCE, visitor);
            }
            for (Map.Entry<String, Role> roleEntry : roleManager.roles().entrySet()) {
                if (roleEntry.getValue().players().contains(uuid)) {
                    roleEntry.getValue().permissions().visit(this, permission, new RoleContext(roleEntry.getKey(), roleManager.getPriority(roleEntry.getKey()), roleEntry.getValue()), visitor);
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
                prefix + "trusted", list(roleManager.getRole(ClaimRoleManager.TRUSTED).players(), uuid -> uuid("trusted_", uuid, server), "text.itsours.placeholders.trusted"),
                prefix + "settings", settings.toText()
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
