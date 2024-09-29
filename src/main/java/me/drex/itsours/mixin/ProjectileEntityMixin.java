package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

import static me.drex.message.api.LocalizedMessage.localized;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity {

    public ProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    protected abstract void onEntityHit(EntityHitResult entityHitResult);

    @Redirect(
        method = "onCollision",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/projectile/ProjectileEntity;onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V"
        )
    )
    public void itsours$canDamageEntity(ProjectileEntity entity, EntityHitResult hitResult) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(hitResult.getEntity());
        if (claim.isEmpty() || !(entity.getOwner() instanceof ServerPlayerEntity)) {
            this.onEntityHit(hitResult);
            return;
        }
        if (!claim.get().checkAction(null, Flags.PVP) && hitResult.getEntity() instanceof PlayerEntity) {
            if (entity.getOwner() instanceof PlayerEntity player) {
                player.sendMessage(localized("text.itsours.action.disallowed.damage_player"), true);
            }
            if (entity instanceof PersistentProjectileEntity) {
                if (((PersistentProjectileEntity) entity).getPierceLevel() > 0) entity.kill((ServerWorld) entity.getWorld());
            }
            return;
        }
        if (!claim.get().checkAction(entity.getOwner().getUuid(), Flags.DAMAGE_ENTITY, Node.registry(Registries.ENTITY_TYPE, hitResult.getEntity().getType())) && !(hitResult.getEntity() instanceof PlayerEntity)) {
            if (entity.getOwner() instanceof PlayerEntity player) {
                player.sendMessage(localized("text.itsours.action.disallowed.damage_entity"), true);
            }
            if (entity instanceof PersistentProjectileEntity) {
                if (((PersistentProjectileEntity) entity).getPierceLevel() > 0) entity.kill((ServerWorld) entity.getWorld());
            }
            return;
        }
        this.onEntityHit(hitResult);
    }

}
