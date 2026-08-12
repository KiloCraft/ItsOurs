package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

import static me.drex.itsours.util.LocalizedMessage.localized;
import static me.drex.itsours.util.LocalizedMessage.sendMessage;
import static me.drex.itsours.claim.flags.Flags.DAMAGE_ENTITY;
import static me.drex.itsours.claim.flags.Flags.PVP;

@Mixin(Projectile.class)
public abstract class ProjectileMixin extends Entity {

    public ProjectileMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Shadow
    protected abstract void onHitEntity(EntityHitResult entityHitResult);

    @Redirect(
        method = "onHit",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/Projectile;onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V"
        )
    )
    public void itsours$canDamageEntity(Projectile entity, EntityHitResult hitResult) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(hitResult.getEntity());
        if (claim.isEmpty() || !(entity.getOwner() instanceof ServerPlayer)) {
            this.onHitEntity(hitResult);
            return;
        }
        if (!claim.get().checkAction(null, PVP) && hitResult.getEntity() instanceof Player) {
            if (entity.getOwner() instanceof Player player) {
                sendMessage(player, localized("text.itsours.action.disallowed.damage_player", player), true);
            }
            if (entity instanceof AbstractArrow) {
                if (((AbstractArrow) entity).getPierceLevel() > 0) entity.kill((ServerLevel) entity.level());
            }
            return;
        }
        if (!claim.get().checkAction(entity.getOwner().getUUID(), DAMAGE_ENTITY, Node.registry(BuiltInRegistries.ENTITY_TYPE, hitResult.getEntity().getType())) && !(hitResult.getEntity() instanceof Player)) {
            if (entity.getOwner() instanceof Player player) {
                sendMessage(player, localized("text.itsours.action.disallowed.damage_entity", player), true);
            }
            if (entity instanceof AbstractArrow) {
                if (((AbstractArrow) entity).getPierceLevel() > 0) entity.kill((ServerLevel) entity.level());
            }
            return;
        }
        this.onHitEntity(hitResult);
    }

}
