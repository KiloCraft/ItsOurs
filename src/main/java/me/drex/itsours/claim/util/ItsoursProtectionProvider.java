package me.drex.itsours.claim.util;

import com.mojang.authlib.GameProfile;
import eu.pb4.common.protection.api.ProtectionProvider;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ItsoursProtectionProvider implements ProtectionProvider {

    public static final ItsoursProtectionProvider INSTANCE = new ItsoursProtectionProvider();

    private ItsoursProtectionProvider() {
    }

    @Override
    public boolean isProtected(Level world, BlockPos pos) {
        if (world instanceof ServerLevel serverWorld) {
            Optional<AbstractClaim> optional = ClaimList.getClaimAt(serverWorld, pos);
            return optional.isPresent();
        }
        return false;
    }

    @Override
    public boolean isAreaProtected(Level world, AABB area) {
        for (AbstractClaim claim : ClaimList.getClaims()) {
            if (claim.getBox().contains(
                new BoundingBox(
                    Mth.floor(area.minX),
                    Mth.floor(area.minY),
                    Mth.floor(area.minZ),
                    Mth.floor(area.maxX),
                    Mth.floor(area.maxY),
                    Mth.floor(area.maxZ))
            )) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canBreakBlock(Level world, BlockPos pos, GameProfile profile, @Nullable Player player) {
        if (world instanceof ServerLevel serverWorld) {
            Optional<AbstractClaim> optional = ClaimList.getClaimAt(serverWorld, pos);
            return optional.map(claim -> claim.checkAction(profile.id(), Flags.MINE)).orElse(true);
        }
        return false;
    }

    @Override
    public boolean canExplodeBlock(Level world, BlockPos pos, Explosion explosion, GameProfile profile, @Nullable Player player) {
        if (world instanceof ServerLevel serverWorld) {
            Optional<AbstractClaim> optional = ClaimList.getClaimAt(serverWorld, pos);
            return optional.map(claim -> claim.checkAction(null, Flags.EXPLOSIONS)).orElse(true);
        }
        return false;
    }

    @Override
    public boolean canPlaceBlock(Level world, BlockPos pos, GameProfile profile, @Nullable Player player) {
        if (world instanceof ServerLevel serverWorld) {
            Optional<AbstractClaim> optional = ClaimList.getClaimAt(serverWorld, pos);
            return optional.map(claim -> claim.checkAction(profile.id(), Flags.PLACE)).orElse(true);
        }
        return false;
    }

    @Override
    public boolean canInteractBlock(Level world, BlockPos pos, GameProfile profile, @Nullable Player player) {
        if (world instanceof ServerLevel serverWorld) {
            Optional<AbstractClaim> optional = ClaimList.getClaimAt(serverWorld, pos);
            return optional.map(claim -> claim.checkAction(profile.id(), Flags.INTERACT_BLOCK)).orElse(true);
        }
        return false;
    }

    @Override
    public boolean canInteractEntity(Level world, Entity entity, GameProfile profile, @Nullable Player player) {
        if (world instanceof ServerLevel serverWorld) {
            Optional<AbstractClaim> optional = ClaimList.getClaimAt(serverWorld, entity.blockPosition());
            return optional.map(claim -> claim.checkAction(profile.id(), Flags.INTERACT_ENTITY, Node.registry(BuiltInRegistries.ENTITY_TYPE, entity.getType()))).orElse(true);
        }
        return false;
    }

    @Override
    public boolean canDamageEntity(Level world, Entity entity, GameProfile profile, @Nullable Player player) {
        if (world instanceof ServerLevel serverWorld) {
            Optional<AbstractClaim> optional = ClaimList.getClaimAt(serverWorld, entity.blockPosition());
            return optional.map(claim -> claim.checkAction(profile.id(), Flags.DAMAGE_ENTITY, Node.registry(BuiltInRegistries.ENTITY_TYPE, entity.getType()))).orElse(true);
        }
        return false;
    }
}
