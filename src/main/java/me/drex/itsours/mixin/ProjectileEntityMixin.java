package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.util.Permission;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity {

    @Shadow protected abstract void onEntityHit(EntityHitResult entityHitResult);

    public ProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Redirect(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V"))
    public void itsours$onCollision(ProjectileEntity entity, EntityHitResult hitResult) {
        AbstractClaim claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) hitResult.getEntity().getEntityWorld(), hitResult.getEntity().getBlockPos());
        if (claim == null || !(entity.getOwner() instanceof ServerPlayerEntity)) {
            this.onEntityHit(hitResult);
            return;
        }
        if (!claim.hasPermission(entity.getOwner().getUuid(), "damage_entity." + Permission.toString(hitResult.getEntity().getType()))) {
            ClaimPlayer claimPlayer = (ClaimPlayer) entity.getOwner();
            claimPlayer.sendError(Component.text("You can't damage that entity here.").color(Color.RED));
            if (entity instanceof PersistentProjectileEntity) {
                if (((PersistentProjectileEntity) entity).getPierceLevel() > 0) entity.kill();
            }
            return;
        }
        this.onEntityHit(hitResult);
    }

}
