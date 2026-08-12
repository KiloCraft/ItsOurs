package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.util.FlagBuilderUtil;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static me.drex.itsours.util.LocalizedMessage.localized;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {

    public BlockPos pos;
    @Shadow
    @Final
    protected ServerPlayer player;
    @Shadow
    protected ServerLevel level;

    @WrapOperation(
        method = "useItemOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;useItemOn(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"
        )
    )
    private InteractionResult itsours$canInteractBlockItemSpecific(BlockState blockState, ItemStack itemStack, Level world, Player playerEntity, InteractionHand hand, BlockHitResult hit, Operation<InteractionResult> original) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, hit.getBlockPos());
        if (claim.isEmpty() || !FlagBuilderUtil.INTERACT_BLOCK_PREDICATE.test(blockState.getBlock()))
            return original.call(blockState, itemStack, world, playerEntity, hand, hit);
        if (!claim.get().checkAction(playerEntity.getUUID(), Flags.INTERACT_BLOCK, Node.registry(BuiltInRegistries.BLOCK, blockState.getBlock()))) {
            player.sendSystemMessage(localized("text.itsours.action.disallowed.interact_block", player), true);
            return InteractionResult.PASS;
        }
        return original.call(blockState, itemStack, world, playerEntity, hand, hit);
    }

    @WrapOperation(
        method = "useItemOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;useWithoutItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"
        )
    )
    private InteractionResult itsours$canInteractBlockDefault(BlockState blockState, Level world, Player playerEntity, BlockHitResult hit, Operation<InteractionResult> original) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, hit.getBlockPos());
        if (claim.isEmpty() || !FlagBuilderUtil.INTERACT_BLOCK_PREDICATE.test(blockState.getBlock()))
            return original.call(blockState, world, playerEntity, hit);
        if (!claim.get().checkAction(playerEntity.getUUID(), Flags.INTERACT_BLOCK, Node.registry(BuiltInRegistries.BLOCK, blockState.getBlock()))) {
            player.sendSystemMessage(localized("text.itsours.action.disallowed.interact_block", player), true);
            return InteractionResult.FAIL;
        }
        return original.call(blockState, world, playerEntity, hit);
    }

    @WrapOperation(
        method = "handleBlockBreakAction",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;attack(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;)V"
        )
    )
    private void itsours$canInteractBlock2(BlockState blockState, Level world, BlockPos pos, Player playerEntity, Operation<Void> original) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, pos);
        if (claim.isEmpty() || !FlagBuilderUtil.INTERACT_BLOCK_PREDICATE.test(blockState.getBlock())) {
            original.call(blockState, world, pos, playerEntity);
            return;
        }
        if (!claim.get().checkAction(playerEntity.getUUID(), Flags.INTERACT_BLOCK, Node.registry(BuiltInRegistries.BLOCK, blockState.getBlock()))) {
            player.sendSystemMessage(localized("text.itsours.action.disallowed.interact_block", player), true);
            return;
        }
        original.call(blockState, world, pos, playerEntity);
    }

    @WrapOperation(
        method = "useItemOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"
        )
    )
    private InteractionResult itsours$canUseOnBlock(ItemStack itemStack, UseOnContext context, Operation<InteractionResult> original) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(context);
        if (claim.isEmpty() || !FlagBuilderUtil.USE_ON_BLOCK_PREDICATE.test(itemStack.getItem()))
            return original.call(itemStack, context);
        if (!claim.get().checkAction(player.getUUID(), Flags.USE_ON_BLOCK, Node.registry(BuiltInRegistries.ITEM, itemStack.getItem()))) {
            player.sendSystemMessage(localized("text.itsours.action.disallowed.interact_item_on_block", player), true);
            return InteractionResult.FAIL;
        }
        return original.call(itemStack, context);
    }

}
