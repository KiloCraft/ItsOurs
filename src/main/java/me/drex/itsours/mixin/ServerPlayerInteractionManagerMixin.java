package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.FlagsManager;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static me.drex.message.api.LocalizedMessage.localized;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {

    public BlockPos pos;
    @Shadow
    @Final
    protected ServerPlayerEntity player;
    @Shadow
    protected ServerWorld world;

    @ModifyExpressionValue(
        method = "tryBreakBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;isBlockBreakingRestricted(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/GameMode;)Z"
        )
    )
    private boolean itsours$canBreakBlock(boolean original, BlockPos pos) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, pos);
        if (claim.isEmpty()) return original;
        if (!claim.get().checkAction(this.player.getUuid(), FlagsManager.MINE, Node.registry(Registries.BLOCK, this.world.getBlockState(pos).getBlock()))) {
            player.sendMessage(localized("text.itsours.action.disallowed.break_block"), true);
            return true;
        }
        return original;
    }

    @WrapOperation(
        method = "interactBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;onUseWithItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ItemActionResult;"
        )
    )
    private ItemActionResult itsours$canInteractBlockItemSpecific(BlockState blockState, ItemStack itemStack, World world, PlayerEntity playerEntity, Hand hand, BlockHitResult hit, Operation<ItemActionResult> original) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, hit.getBlockPos());
        if (claim.isEmpty() || !FlagsManager.INTERACT_BLOCK_PREDICATE.test(blockState.getBlock()))
            return original.call(blockState, itemStack, world, playerEntity, hand, hit);
        if (!claim.get().checkAction(playerEntity.getUuid(), FlagsManager.INTERACT_BLOCK, Node.registry(Registries.BLOCK, blockState.getBlock()))) {
            player.sendMessage(localized("text.itsours.action.disallowed.interact_block"), true);
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return original.call(blockState, itemStack, world, playerEntity, hand, hit);
    }

    @WrapOperation(
        method = "interactBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"
        )
    )
    private ActionResult itsours$canInteractBlockDefault(BlockState blockState, World world, PlayerEntity playerEntity, BlockHitResult hit, Operation<ActionResult> original) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, hit.getBlockPos());
        if (claim.isEmpty() || !FlagsManager.INTERACT_BLOCK_PREDICATE.test(blockState.getBlock()))
            return original.call(blockState, world, playerEntity, hit);
        if (!claim.get().checkAction(playerEntity.getUuid(), FlagsManager.INTERACT_BLOCK, Node.registry(Registries.BLOCK, blockState.getBlock()))) {
            player.sendMessage(localized("text.itsours.action.disallowed.interact_block"), true);
            return ActionResult.FAIL;
        }
        return original.call(blockState, world, playerEntity, hit);
    }

    @WrapOperation(
        method = "processBlockBreakingAction",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;onBlockBreakStart(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;)V"
        )
    )
    private void itsours$canInteractBlock2(BlockState blockState, World world, BlockPos pos, PlayerEntity playerEntity, Operation<Void> original) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, pos);
        if (claim.isEmpty() || !FlagsManager.INTERACT_BLOCK_PREDICATE.test(blockState.getBlock())) {
            original.call(blockState, world, pos, playerEntity);
            return;
        }
        if (!claim.get().checkAction(playerEntity.getUuid(), FlagsManager.INTERACT_BLOCK, Node.registry(Registries.BLOCK, blockState.getBlock()))) {
            player.sendMessage(localized("text.itsours.action.disallowed.interact_block"), true);
            return;
        }
        original.call(blockState, world, pos, playerEntity);
    }

    @WrapOperation(
        method = "interactBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"
        )
    )
    private ActionResult itsours$canUseOnBlock(ItemStack itemStack, ItemUsageContext context, Operation<ActionResult> original) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(context);
        if (claim.isEmpty() || !FlagsManager.USE_ON_BLOCK_PREDICATE.test(itemStack.getItem()))
            return original.call(itemStack, context);
        if (!claim.get().checkAction(player.getUuid(), FlagsManager.USE_ON_BLOCK, Node.registry(Registries.ITEM, itemStack.getItem()))) {
            player.sendMessage(localized("text.itsours.action.disallowed.interact_item_on_block"), true);
            return ActionResult.FAIL;
        }
        return original.call(itemStack, context);
    }

}
