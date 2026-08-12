package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.util.ClaimFlags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static me.drex.itsours.util.LocalizedMessage.localized;
import static me.drex.itsours.util.LocalizedMessage.sendMessage;
import static me.drex.itsours.claim.flags.Flags.DAMAGE_ENTITY;
import static me.drex.itsours.claim.flags.Flags.GLIDE;
import static me.drex.itsours.claim.flags.Flags.INTERACT_ENTITY;
import static me.drex.itsours.claim.flags.Flags.PVP;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(
        method = "cannotAttack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;isAttackable()Z"
        )
    )
    private boolean itsours$canDamageEntity(boolean original, Entity entity) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(entity.level(), entity.blockPosition());
        if (claim.isEmpty()) {
            return original;
        }
        if (!claim.get().checkAction(null, PVP) && entity instanceof Player) {
            sendMessage((Player) (Object) this, localized("text.itsours.action.disallowed.damage_player", (Player) (Object) this), true);
            return false;
        }
        if (!claim.get().checkAction(this.getUUID(), DAMAGE_ENTITY, Node.registry(BuiltInRegistries.ENTITY_TYPE, entity.getType())) && !(entity instanceof Player)) {
            sendMessage((Player) (Object) this, localized("text.itsours.action.disallowed.damage_entity", (Player) (Object) this), true);
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(
        method = "doSweepAttack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;distanceToSqr(Lnet/minecraft/world/entity/Entity;)D"
        )
    )
    public double itsours$shouldApplySweepingDamage(double original, Entity entity) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(entity);
        if (claim.isEmpty()) {
            return original;
        }
        if (!claim.get().checkAction(null, PVP) && entity instanceof Player) {
            sendMessage((Player) (Object) this, localized("text.itsours.action.disallowed.damage_player", (Player) (Object) this), true);
            return Double.MAX_VALUE;
        }
        if (!claim.get().checkAction(this.getUUID(), DAMAGE_ENTITY, Node.registry(BuiltInRegistries.ENTITY_TYPE, entity.getType())) && !(entity instanceof Player)) {
            sendMessage((Player) (Object) this, localized("text.itsours.action.disallowed.damage_entity", (Player) (Object) this), true);
            return Double.MAX_VALUE;
        }
        return original;
    }

    @WrapOperation(
        method = "interactOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/InteractionResult;"
        )
    )
    private InteractionResult itsours$canInteractEntity(Entity entity, Player player, InteractionHand hand, Vec3 hitPosition, Operation<InteractionResult> original) {
        // special case to allow owners to interact with their entities
        if (entity instanceof OwnableEntity tameable && tameable.getOwner() == player) {
            return original.call(entity, player, hand, hitPosition);
        }
        return ClaimFlags.check(
            this,
            "text.itsours.action.disallowed.interact_entity",
            () -> InteractionResult.FAIL,
            () -> original.call(entity, player, hand, hitPosition),
            INTERACT_ENTITY, Node.registry(BuiltInRegistries.ENTITY_TYPE, entity.getType())
        );
    }

    @WrapOperation(
        method = "tryToStartFallFlying",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;canGlide()Z"
        )
    )
    private boolean itsours$canStartGliding(Player instance, Operation<Boolean> original) {
        return ClaimFlags.check(
            this,
            "text.itsours.action.disallowed.elytra",
            () -> false,
            () -> original.call(instance),
            GLIDE
        );
    }

}
