package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Shadow
    protected ServerWorld world;

    public BlockPos pos;


    @ModifyExpressionValue(
            method = "tryBreakBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;isBlockBreakingRestricted(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/GameMode;)Z"
            )
    )
    private boolean itsours$canBreakBlock(boolean original, BlockPos pos) {
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt(world, pos);
        if (claim.isEmpty()) return original;
        if (!claim.get().hasPermission(this.player.getUuid(), PermissionManager.MINE, Node.dummy(Registry.BLOCK, this.world.getBlockState(pos).getBlock()))) {
            player.sendMessage(Text.translatable("text.itsours.action.disallowed.break_block").formatted(Formatting.RED), true);
            return true;
        }
        return original;
    }

    @WrapOperation(
            method = "interactBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"
            )
    )
    private ActionResult itsours$canInteractBlock(BlockState blockState, World world, PlayerEntity playerEntity, Hand hand, BlockHitResult hit, Operation<ActionResult> original) {
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt((ServerWorld) world, hit.getBlockPos());
        if (claim.isEmpty() || !PermissionManager.INTERACT_BLOCK_PREDICATE.test(blockState.getBlock()))
            return original.call(blockState, world, playerEntity, hand, hit);
        if (!claim.get().hasPermission(playerEntity.getUuid(), PermissionManager.INTERACT_BLOCK, Node.dummy(Registry.BLOCK, blockState.getBlock()))) {
            player.sendMessage(Text.translatable("text.itsours.action.disallowed.interact_block").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }
        return original.call(blockState, world, playerEntity, hand, hit);
    }

    @WrapOperation(
            method = "interactBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"
            )
    )
    private ActionResult itsours$canUseOnBlock(ItemStack itemStack, ItemUsageContext context, Operation<ActionResult> original) {
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt(context);
        if (claim.isEmpty() || !PermissionManager.USE_ON_BLOCK_PREDICATE.test(itemStack.getItem()))
            return original.call(itemStack, context);
        if (!claim.get().hasPermission(player.getUuid(), PermissionManager.USE_ON_BLOCK, Node.dummy(Registry.ITEM, itemStack.getItem()))) {
            player.sendMessage(Text.translatable("text.itsours.action.disallowed.interact_item_on_block").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }
        return original.call(itemStack, context);
    }

}
