package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.claim.permission.util.Misc;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static me.drex.message.api.LocalizedMessage.localized;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract void sendMessage(Text message, boolean actionBar);

    @ModifyExpressionValue(
        method = "attack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;isAttackable()Z"
        )
    )
    private boolean itsours$canDamageEntity(boolean original, Entity entity) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt((ServerWorld) entity.getEntityWorld(), entity.getBlockPos());
        if (claim.isEmpty()) {
            return original;
        }
        if (!claim.get().hasPermission(null, PermissionManager.PVP) && entity instanceof PlayerEntity) {
            this.sendMessage(localized("text.itsours.action.disallowed.damage_player"), true);
            return false;
        }
        if (!claim.get().hasPermission(this.getUuid(), PermissionManager.DAMAGE_ENTITY, Node.registry(Registries.ENTITY_TYPE, entity.getType())) && !(entity instanceof PlayerEntity)) {
            this.sendMessage(localized("text.itsours.action.disallowed.damage_entity"), true);
            return false;
        }
        return original;
    }

    @ModifyExpressionValue(
        method = "attack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/player/PlayerEntity;squaredDistanceTo(Lnet/minecraft/entity/Entity;)D"
        )
    )
    public double itsours$shouldApplySweepingDamage(double original, Entity entity) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(entity);
        if (claim.isEmpty()) {
            return original;
        }
        if (!claim.get().hasPermission(null, PermissionManager.PVP) && entity instanceof PlayerEntity) {
            this.sendMessage(localized("text.itsours.action.disallowed.damage_player"), true);
            return Double.MAX_VALUE;
        }
        if (!claim.get().hasPermission(this.getUuid(), PermissionManager.DAMAGE_ENTITY, Node.registry(Registries.ENTITY_TYPE, entity.getType())) && !(entity instanceof PlayerEntity)) {
            this.sendMessage(localized("text.itsours.action.disallowed.damage_entity"), true);
            return Double.MAX_VALUE;
        }
        return original;
    }

    @WrapOperation(
        method = "interact",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"
        )
    )
    private ActionResult itsours$canInteractEntity(Entity entity, PlayerEntity player, Hand hand, Operation<ActionResult> original) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(entity);
        if (claim.isEmpty())
            return original.call(entity, player, hand);
        if (!claim.get().hasPermission(this.getUuid(), PermissionManager.INTERACT_ENTITY, Node.registry(Registries.ENTITY_TYPE, entity.getType()))) {
            player.sendMessage(localized("text.itsours.action.disallowed.interact_entity"), true);
            return ActionResult.FAIL;
        }
        return original.call(entity, player, hand);
    }

    @WrapOperation(
        method = "checkFallFlying",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ElytraItem;isUsable(Lnet/minecraft/item/ItemStack;)Z"
        )
    )
    private boolean itsours$canStartGliding(ItemStack itemStack, Operation<Boolean> original) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt((PlayerEntity) (Object) this);
        if (claim.isEmpty())
            return original.call(itemStack);
        if (!claim.get().hasPermission(this.getUuid(), PermissionManager.MISC, Misc.ELYTRA.node())) {
            this.sendMessage(localized("text.itsours.action.disallowed.misc.elytra"), true);
            return false;
        }
        return original.call(itemStack);
    }

}
