package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin extends Entity {

    public ProjectileEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    protected abstract void onEntityHit(EntityHitResult entityHitResult);

    @Redirect(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V"))
    public void itsours$onCollision(ProjectileEntity entity, EntityHitResult hitResult) {
        Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) hitResult.getEntity().getEntityWorld(), hitResult.getEntity().getBlockPos());
        if (!claim.isPresent() || !(entity.getOwner() instanceof ServerPlayerEntity)) {
            this.onEntityHit(hitResult);
            return;
        }
        ClaimPlayer claimPlayer = (ClaimPlayer) entity.getOwner();
        if (!claim.get().getSetting("pvp") && hitResult.getEntity() instanceof PlayerEntity) {
            claimPlayer.sendError(Component.text("You can't pvp here.").color(Color.RED));
            if (entity instanceof PersistentProjectileEntity) {
                if (((PersistentProjectileEntity) entity).getPierceLevel() > 0) entity.kill();
            }
            return;
        }
        if (!claim.get().hasPermission(entity.getOwner().getUuid(), "damage_entity." + Registry.ENTITY_TYPE.getId(hitResult.getEntity().getType()).getPath()) && !(hitResult.getEntity() instanceof PlayerEntity)) {
            claimPlayer.sendError(Component.text("You can't damage that entity here.").color(Color.RED));
            if (entity instanceof PersistentProjectileEntity) {
                if (((PersistentProjectileEntity) entity).getPierceLevel() > 0) entity.kill();
            }
            return;
        }
        this.onEntityHit(hitResult);
    }

}
