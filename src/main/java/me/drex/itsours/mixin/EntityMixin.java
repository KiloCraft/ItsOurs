package me.drex.itsours.mixin;

import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin {

    public AbstractClaim pclaim = null;

    @Inject(method = "setPos", at = @At("RETURN"))
    public void itsours$doPostPosActions(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity player) {
            if (player.getBlockPos() == null) return;
            AbstractClaim claim = ClaimList.INSTANCE.getClaimAt(player).orElse(null);
            if (!Objects.equals(pclaim, claim)) {
                if (player.networkHandler != null) {
                    if (pclaim != null) pclaim.onLeave(claim, player);
                    if (claim != null) claim.onEnter(pclaim, player);
                }
            }
            pclaim = claim;
        }
    }

    @Redirect(method = "checkBlockCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V"))
    private void itsours$onBlockCollision(BlockState blockState, World world, BlockPos pos, Entity entity) {
        ServerPlayerEntity playerEntity = null;
        if (entity instanceof ProjectileEntity projectileEntity && (blockState.getBlock() instanceof AbstractButtonBlock || blockState.getBlock() instanceof AbstractPressurePlateBlock)) {
            if (projectileEntity.getOwner() != null && projectileEntity.getOwner() instanceof ServerPlayerEntity) {
                playerEntity = (ServerPlayerEntity) projectileEntity.getOwner();
            }
        } else if (entity instanceof ServerPlayerEntity && blockState.getBlock() instanceof AbstractPressurePlateBlock) {
            playerEntity = (ServerPlayerEntity) entity;
        } else if (entity instanceof ItemEntity item && blockState.getBlock() instanceof AbstractPressurePlateBlock) {
            if (item.getThrower() != null) {
                playerEntity = ItsOurs.INSTANCE.server.getPlayerManager().getPlayer(item.getThrower());
            }
        }
        if (playerEntity == null) {
            blockState.onEntityCollision(world, pos, entity);
            return;
        }
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt((ServerWorld) world, pos);
        if (claim.isEmpty()) {
            blockState.onEntityCollision(world, pos, entity);
            return;
        }
        if (!claim.get().hasPermission(playerEntity.getUuid(), PermissionManager.INTERACT_BLOCK, Node.dummy(Registry.BLOCK, blockState.getBlock()))) {
            playerEntity.sendMessage(Text.translatable("text.itsours.action.disallowed.interact_block").formatted(Formatting.RED));
            return;
        }
        blockState.onEntityCollision(world, pos, entity);
    }

}
