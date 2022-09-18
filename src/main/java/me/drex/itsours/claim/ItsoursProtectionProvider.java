package me.drex.itsours.claim;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.protection.api.ProtectionProvider;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ItsoursProtectionProvider implements ProtectionProvider {

    public static final ItsoursProtectionProvider INSTANCE = new ItsoursProtectionProvider();

    private ItsoursProtectionProvider() {
    }

    @Override
    public boolean isProtected(World world, BlockPos pos) {
        if (world instanceof ServerWorld serverWorld) {
            Optional<AbstractClaim> optional = ClaimList.INSTANCE.getClaimAt(serverWorld, pos);
            return optional.isPresent();
        }
        return false;
    }

    @Override
    public boolean isAreaProtected(World world, Box area) {
        for (AbstractClaim claim : ClaimList.INSTANCE.getClaims()) {
            if (claim.getBox().contains(
                    new BlockBox(
                            MathHelper.floor(area.minX),
                            MathHelper.floor(area.minY),
                            MathHelper.floor(area.minZ),
                            MathHelper.floor(area.maxX),
                            MathHelper.floor(area.maxY),
                            MathHelper.floor(area.maxZ))
            )) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canBreakBlock(World world, BlockPos pos, GameProfile profile, @Nullable PlayerEntity player) {
        if (world instanceof ServerWorld serverWorld) {
            Optional<AbstractClaim> optional = ClaimList.INSTANCE.getClaimAt(serverWorld, pos);
            return optional.isPresent() && optional.get().hasPermission(profile.getId(), PermissionManager.MINE);
        }
        return false;
    }

    @Override
    public boolean canExplodeBlock(World world, BlockPos pos, Explosion explosion, GameProfile profile, @Nullable PlayerEntity player) {
        if (world instanceof ServerWorld serverWorld) {
            Optional<AbstractClaim> optional = ClaimList.INSTANCE.getClaimAt(serverWorld, pos);
            return optional.isPresent() && optional.get().hasPermission(null, PermissionManager.EXPLOSIONS);
        }
        return false;
    }

    @Override
    public boolean canPlaceBlock(World world, BlockPos pos, GameProfile profile, @Nullable PlayerEntity player) {
        if (world instanceof ServerWorld serverWorld) {
            Optional<AbstractClaim> optional = ClaimList.INSTANCE.getClaimAt(serverWorld, pos);
            return optional.isPresent() && optional.get().hasPermission(profile.getId(), PermissionManager.PLACE);
        }
        return false;
    }

    @Override
    public boolean canInteractBlock(World world, BlockPos pos, GameProfile profile, @Nullable PlayerEntity player) {
        if (world instanceof ServerWorld serverWorld) {
            Optional<AbstractClaim> optional = ClaimList.INSTANCE.getClaimAt(serverWorld, pos);
            return optional.isPresent() && optional.get().hasPermission(profile.getId(), PermissionManager.INTERACT_BLOCK);
        }
        return false;
    }

    @Override
    public boolean canInteractEntity(World world, Entity entity, GameProfile profile, @Nullable PlayerEntity player) {
        if (world instanceof ServerWorld serverWorld) {
            Optional<AbstractClaim> optional = ClaimList.INSTANCE.getClaimAt(serverWorld, entity.getBlockPos());
            return optional.isPresent() && optional.get().hasPermission(profile.getId(), PermissionManager.INTERACT_ENTITY, Node.dummy(Registry.ENTITY_TYPE, entity.getType()));
        }
        return false;
    }

    @Override
    public boolean canDamageEntity(World world, Entity entity, GameProfile profile, @Nullable PlayerEntity player) {
        if (world instanceof ServerWorld serverWorld) {
            Optional<AbstractClaim> optional = ClaimList.INSTANCE.getClaimAt(serverWorld, entity.getBlockPos());
            return optional.isPresent() && optional.get().hasPermission(profile.getId(), PermissionManager.DAMAGE_ENTITY, Node.dummy(Registry.ENTITY_TYPE, entity.getType()));
        }
        return false;
    }
}
