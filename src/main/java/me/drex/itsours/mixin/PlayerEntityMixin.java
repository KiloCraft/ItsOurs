package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Shadow public abstract void sendMessage(Text message, boolean actionBar);

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;isAttackable()Z"
            )
    )
    private boolean canDamageEntity(Entity entity) {
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt((ServerWorld) entity.getEntityWorld(), entity.getBlockPos());
        if (claim.isEmpty()) {
            return entity.isAttackable();
        }
        if (!claim.get().hasPermission(null, PermissionManager.PVP) && entity instanceof PlayerEntity) {
            this.sendMessage(Text.translatable("text.itsours.action.disallowed.damage_player").formatted(Formatting.RED));
            return false;
        }
        if (!claim.get().hasPermission(this.getUuid(), PermissionManager.DAMAGE_ENTITY, Node.dummy(Registry.ENTITY_TYPE, entity.getType())) && !(entity instanceof PlayerEntity)) {
            this.sendMessage(Text.translatable("text.itsours.action.disallowed.damage_entity").formatted(Formatting.RED));
            return false;
        }
        return entity.isAttackable();
    }

    @Redirect(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    public boolean shouldApplySweepingDamage(LivingEntity livingEntity, DamageSource source, float amount) {
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt(livingEntity);
        if (claim.isEmpty()) {
            return livingEntity.damage(DamageSource.player((PlayerEntity) (Object)this), amount);
        }
        if (!claim.get().hasPermission(null, PermissionManager.PVP) && livingEntity instanceof PlayerEntity) {
            this.sendMessage(Text.translatable("text.itsours.action.disallowed.damage_player").formatted(Formatting.RED));
            return false;
        }
        if (!claim.get().hasPermission(this.getUuid(), PermissionManager.DAMAGE_ENTITY, Node.dummy(Registry.ENTITY_TYPE, livingEntity.getType())) && !(livingEntity instanceof PlayerEntity)) {
            this.sendMessage(Text.translatable("text.itsours.action.disallowed.damage_entity").formatted(Formatting.RED));
            return false;
        }
        return livingEntity.damage(DamageSource.player((PlayerEntity) (Object)this), amount);
    }

    @Redirect(
            method = "interact",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"
            )
    )
    private ActionResult canInteractEntity(Entity entity, PlayerEntity player, Hand hand) {
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt(entity);
        if (claim.isEmpty())
            return entity.interact(player, hand);
        if (!claim.get().hasPermission(this.getUuid(), PermissionManager.INTERACT_ENTITY, Node.dummy(Registry.ENTITY_TYPE, entity.getType())))
        {
            player.sendMessage(Text.translatable("text.itsours.action.disallowed.interact_entity").formatted(Formatting.RED));
            return ActionResult.FAIL;
        }
        return entity.interact(player, hand);
    }

}
