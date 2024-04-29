package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Optional;

import static me.drex.message.api.LocalizedMessage.localized;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract EntityType<?> getType();

    public AbstractClaim pclaim = null;

    @Inject(method = "setPos", at = @At("RETURN"))
    public void itsours$doPostPosActions(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity player) {
            if (player.getBlockPos() == null) return;
            AbstractClaim claim = ClaimList.getClaimAt(player).orElse(null);
            if (!Objects.equals(pclaim, claim)) {
                if (player.networkHandler != null) {
                    if (pclaim != null) pclaim.onLeave(claim, player);
                    if (claim != null) claim.onEnter(pclaim, player);
                }
            }
            pclaim = claim;
        }
    }

    @WrapWithCondition(
        method = "checkBlockCollision",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;onEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V"
        )
    )
    private boolean itsours$onBlockCollision(BlockState blockState, World world, BlockPos pos, Entity entity) {
        ServerPlayerEntity playerEntity = null;
        if (entity instanceof ProjectileEntity projectileEntity && (blockState.getBlock() instanceof ButtonBlock || blockState.getBlock() instanceof AbstractPressurePlateBlock)) {
            if (projectileEntity.getOwner() != null && projectileEntity.getOwner() instanceof ServerPlayerEntity) {
                playerEntity = (ServerPlayerEntity) projectileEntity.getOwner();
            }
        } else if (entity instanceof ServerPlayerEntity && blockState.getBlock() instanceof AbstractPressurePlateBlock) {
            playerEntity = (ServerPlayerEntity) entity;
        } else if (entity instanceof ItemEntity item && blockState.getBlock() instanceof AbstractPressurePlateBlock) {
            if (item.getOwner() != null && item.getOwner() instanceof ServerPlayerEntity owner) {
                playerEntity = owner;
            }
        }
        if (playerEntity == null) {
            return true;
        }
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, pos);
        if (claim.isEmpty()) {
            return true;
        }
        if (!claim.get().hasPermission(playerEntity.getUuid(), PermissionManager.INTERACT_BLOCK, Node.registry(Registries.BLOCK, blockState.getBlock()))) {
            playerEntity.sendMessage(localized("text.itsours.action.disallowed.interact_block"), true);
            return false;
        }
        return true;
    }

    @Inject(
        method = "isImmuneToExplosion",
        at = @At("RETURN"),
        cancellable = true
    )
    private void itsours$preventExplosionImpact(Explosion explosion, CallbackInfoReturnable<Boolean> cir) {
        Entity cause = explosion.getEntity();
        Entity this$entity = (Entity) (Object) this;
        if (cause instanceof Ownable ownable) {
            Entity owner = ownable.getOwner();
            if (owner != null) {
                cause = owner;
            }
        } else if (cause instanceof MobEntity mobEntity) {
            LivingEntity target = mobEntity.getTarget();
            if (target != null) {
                cause = target;
            }
        }
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(this$entity);
        if (claim.isEmpty()) {
            return;
        }
        if (cause != null) {
            if (cause == this$entity) return;
            if (!claim.get().hasPermission(null, PermissionManager.PVP) && this$entity instanceof PlayerEntity) {
                if (cause instanceof PlayerEntity player) {
                    player.sendMessage(localized("text.itsours.action.disallowed.damage_player"), true);
                }
                cir.setReturnValue(true);
                return;
            }
            if (!claim.get().hasPermission(cause.getUuid(), PermissionManager.DAMAGE_ENTITY, Node.registry(Registries.ENTITY_TYPE, this.getType())) && !(this$entity instanceof PlayerEntity)) {
                if (cause instanceof PlayerEntity player) {
                    player.sendMessage(localized("text.itsours.action.disallowed.damage_entity"), true);
                }
                cir.setReturnValue(true);
            }
        } else {
            if (!claim.get().hasPermission(null, PermissionManager.EXPLOSIONS)) {
                cir.setReturnValue(true);
            }
        }
    }


}
