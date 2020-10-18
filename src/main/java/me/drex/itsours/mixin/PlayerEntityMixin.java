package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.util.Group;
import me.drex.itsours.claim.permission.util.Permission;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;C
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isAttackable()Z"))
    private boolean ItsOurs$onDamage(Entity entity) {
        AbstractClaim claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) entity.getEntityWorld(), entity.getBlockPos());
        if (claim == null)
            return entity.isAttackable();
        if (!claim.hasPermission(this.getUuid(), "damage_entity." + Permission.toString(entity.getType()))) {
            ClaimPlayer claimPlayer = (ClaimPlayer) this;
            claimPlayer.sendError(Component.text("You can't damage that entity here.").color(Color.RED));
            return false;
        }
        return entity.isAttackable();
    }

}