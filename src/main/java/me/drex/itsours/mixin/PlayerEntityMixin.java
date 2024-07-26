package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.util.ClaimFlags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
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
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(entity.getEntityWorld(), entity.getBlockPos());
        if (claim.isEmpty()) {
            return original;
        }
        if (!claim.get().checkAction(null, Flags.PVP) && entity instanceof PlayerEntity) {
            this.sendMessage(localized("text.itsours.action.disallowed.damage_player"), true);
            return false;
        }
        if (!claim.get().checkAction(this.getUuid(), Flags.DAMAGE_ENTITY, Node.registry(Registries.ENTITY_TYPE, entity.getType())) && !(entity instanceof PlayerEntity)) {
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
        if (!claim.get().checkAction(null, Flags.PVP) && entity instanceof PlayerEntity) {
            this.sendMessage(localized("text.itsours.action.disallowed.damage_player"), true);
            return Double.MAX_VALUE;
        }
        if (!claim.get().checkAction(this.getUuid(), Flags.DAMAGE_ENTITY, Node.registry(Registries.ENTITY_TYPE, entity.getType())) && !(entity instanceof PlayerEntity)) {
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
        return ClaimFlags.check(
            this, 
            "text.itsours.action.disallowed.interact_entity",
            () -> ActionResult.FAIL, 
            () -> original.call(entity, player, hand), 
            Flags.INTERACT_ENTITY, Node.registry(Registries.ENTITY_TYPE, entity.getType())
        );
    }

    @WrapOperation(
        method = "checkFallFlying",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ElytraItem;isUsable(Lnet/minecraft/item/ItemStack;)Z"
        )
    )
    private boolean itsours$canStartGliding(ItemStack itemStack, Operation<Boolean> original) {
        return ClaimFlags.check(
            this,
            "text.itsours.action.disallowed.elytra",
            () -> false,
            () -> original.call(itemStack),
            Flags.GLIDE
        );
    }

}
