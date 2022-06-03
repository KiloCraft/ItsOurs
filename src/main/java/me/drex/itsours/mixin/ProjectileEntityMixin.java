package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

    @Redirect(
            method = "onCollision",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/projectile/ProjectileEntity;onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V"
            )
    )
    public void canDamageEntity(ProjectileEntity entity, EntityHitResult hitResult) {
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt(hitResult.getEntity());
        if (claim.isEmpty() || !(entity.getOwner() instanceof ServerPlayerEntity)) {
            this.onEntityHit(hitResult);
            return;
        }
        if (!claim.get().hasPermission(null, PermissionManager.PVP) && hitResult.getEntity() instanceof PlayerEntity) {
            entity.getOwner().sendMessage(Text.translatable("text.itsours.action.disallowed.damage_player").formatted(Formatting.RED));
            if (entity instanceof PersistentProjectileEntity) {
                if (((PersistentProjectileEntity) entity).getPierceLevel() > 0) entity.kill();
            }
            return;
        }
        if (!claim.get().hasPermission(entity.getOwner().getUuid(), PermissionManager.DAMAGE_ENTITY, Node.dummy(Registry.ENTITY_TYPE, hitResult.getEntity().getType())) && !(hitResult.getEntity() instanceof PlayerEntity)) {
            entity.getOwner().sendMessage(Text.translatable("text.itsours.action.disallowed.damage_entity").formatted(Formatting.RED));
            if (entity instanceof PersistentProjectileEntity) {
                if (((PersistentProjectileEntity) entity).getPierceLevel() > 0) entity.kill();
            }
            return;
        }
        this.onEntityHit(hitResult);
    }

}
