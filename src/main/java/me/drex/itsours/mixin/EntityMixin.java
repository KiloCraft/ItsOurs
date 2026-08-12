package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Optional;

import static me.drex.itsours.util.LocalizedMessage.localized;
import static me.drex.itsours.util.LocalizedMessage.sendMessage;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow
    public abstract EntityType<?> getType();

    public AbstractClaim pclaim = null;

    @Inject(method = "tick", at = @At("RETURN"))
    public void itsours$onTick(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) {
            if (player.blockPosition() == null) return;
            AbstractClaim claim = ClaimList.getClaimAt(player).orElse(null);
            if (!Objects.equals(pclaim, claim)) {
                if (player.connection != null) {
                    if (pclaim != null) pclaim.onLeave(claim, player);
                    if (claim != null) claim.onEnter(pclaim, player);
                }
            }
            pclaim = claim;
        }
    }

    @WrapWithCondition(
        method = "lambda$checkInsideBlocks$0",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;entityInside(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/InsideBlockEffectApplier;Z)V"
        )
    )
    private boolean itsours$onBlockCollision(BlockState blockState, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier entityCollisionHandler, boolean b) {
        ServerPlayer playerEntity = null;
        if (entity instanceof Projectile projectileEntity && (blockState.getBlock() instanceof ButtonBlock || blockState.getBlock() instanceof BasePressurePlateBlock)) {
            if (projectileEntity.getOwner() != null && projectileEntity.getOwner() instanceof ServerPlayer) {
                playerEntity = (ServerPlayer) projectileEntity.getOwner();
            }
        } else if (entity instanceof ServerPlayer && blockState.getBlock() instanceof BasePressurePlateBlock) {
            playerEntity = (ServerPlayer) entity;
        } else if (entity instanceof ItemEntity item && blockState.getBlock() instanceof BasePressurePlateBlock) {
            if (item.getOwner() != null && item.getOwner() instanceof ServerPlayer owner) {
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
        if (!claim.get().checkAction(playerEntity.getUUID(), Flags.INTERACT_BLOCK, Node.registry(BuiltInRegistries.BLOCK, blockState.getBlock()))) {
            sendMessage(playerEntity, localized("text.itsours.action.disallowed.interact_block", playerEntity), true);
            return false;
        }
        return true;
    }

    @Inject(
        method = "ignoreExplosion",
        at = @At("RETURN"),
        cancellable = true
    )
    private void itsours$preventExplosionImpact(Explosion explosion, CallbackInfoReturnable<Boolean> cir) {
        Entity cause = explosion.getDirectSourceEntity();
        Entity this$entity = (Entity) (Object) this;
        if (cause instanceof TraceableEntity ownable) {
            Entity owner = ownable.getOwner();
            if (owner != null) {
                cause = owner;
            }
        } else if (cause instanceof Mob mobEntity) {
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
            if (!claim.get().checkAction(null, Flags.PVP) && this$entity instanceof Player) {
                if (cause instanceof Player player) {
                    sendMessage(player, localized("text.itsours.action.disallowed.damage_player", player), true);
                }
                cir.setReturnValue(true);
                return;
            }
            if (!claim.get().checkAction(cause.getUUID(), Flags.DAMAGE_ENTITY, Node.registry(BuiltInRegistries.ENTITY_TYPE, this.getType())) && !(this$entity instanceof Player)) {
                if (cause instanceof Player player) {
                    sendMessage(player, localized("text.itsours.action.disallowed.damage_entity", player), true);
                }
                cir.setReturnValue(true);
            }
        } else {
            if (!claim.get().checkAction(null, Flags.EXPLOSIONS)) {
                cir.setReturnValue(true);
            }
        }
    }


}
