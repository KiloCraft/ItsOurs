package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

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
        Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) entity.getEntityWorld(), entity.getBlockPos());
        ClaimPlayer claimPlayer = (ClaimPlayer) this;
        if (!claim.isPresent()) {
            return entity.isAttackable();
        }
        if (!claim.get().getSetting("pvp") && entity instanceof PlayerEntity) {
            claimPlayer.sendError(Component.text("You can't pvp here.").color(Color.RED));
            return false;
        }
        if (!claim.get().hasPermission(this.getUuid(), "damage_entity." + Registry.ENTITY_TYPE.getId(entity.getType()).getPath()) && !(entity instanceof PlayerEntity)) {
            claimPlayer.sendError(Component.text("You can't damage that entity here.").color(Color.RED));
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
        Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) livingEntity.getEntityWorld(), livingEntity.getBlockPos());
        ClaimPlayer claimPlayer = (ClaimPlayer) this;
        if (!claim.isPresent()) {
            return livingEntity.damage(DamageSource.player((PlayerEntity) (Object)this), amount);
        }
        if (!claim.get().getSetting("pvp") && livingEntity instanceof PlayerEntity) {
            claimPlayer.sendError(Component.text("You can't pvp here.").color(Color.RED));
            return false;
        }
        if (!claim.get().hasPermission(this.getUuid(), "damage_entity." + Registry.ENTITY_TYPE.getId(livingEntity.getType()).getPath()) && !(livingEntity instanceof PlayerEntity)) {
            claimPlayer.sendError(Component.text("You can't damage that entity here.").color(Color.RED));
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
        Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) entity.getEntityWorld(), entity.getBlockPos());
        if (claim.isEmpty())
            return entity.interact(player, hand);
        if (!claim.get().hasPermission(this.getUuid(), "interact_entity." + Registry.ENTITY_TYPE.getId(entity.getType()).getPath()))
        {
            ClaimPlayer claimPlayer = (ClaimPlayer) this;
            claimPlayer.sendError(Component.text("You can't interact with that entity here.").color(Color.RED));
            return ActionResult.FAIL;
        }
        return entity.interact(player, hand);
    }

}
