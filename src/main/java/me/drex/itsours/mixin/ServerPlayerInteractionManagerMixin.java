package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionList;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.text.Text;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Shadow
    protected ServerWorld world;

    public BlockPos pos;


    // TODO:
    /*@Redirect(
            method = "processBlockBreakingAction",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;canPlayerModifyAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private boolean onBlockLeftClick(ServerWorld serverWorld, PlayerEntity player, BlockPos pos) {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        if ((player.getInventory().getMainHandStack().getItem() == Items.GOLDEN_SHOVEL || claimPlayer.isSelecting()) && isDifferent(claimPlayer.getLeftPosition(), pos)) {
            claimPlayer.sendMessage(Text.translatable("text.itsours.select.pos1", pos.getX(), pos.getY(), pos.getZ()).formatted(Formatting.GREEN));
            claimPlayer.setLeftPosition(pos);
            onClaimAddCorner();
            return false;
        }
        return true;
    }*/

    /*@Redirect(
            method = "interactBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"
            )
    )
    private boolean onBlockRightClick(ItemStack itemStack) {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        if ((itemStack.getItem() == Items.GOLDEN_SHOVEL || claimPlayer.isSelecting()) && isDifferent(claimPlayer.getRightPosition(), pos)) {
            claimPlayer.sendMessage(Text.translatable("text.itsours.select.pos2", pos.getX(), pos.getY(), pos.getZ()).formatted(Formatting.GREEN));
            claimPlayer.setRightPosition(pos);
            onClaimAddCorner();
            return true;
        }
        return itemStack.isEmpty();
    }*/

    @Redirect(
            method = "tryBreakBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayerEntity;isBlockBreakingRestricted(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/GameMode;)Z"
            )
    )
    private boolean canBreakBlock(ServerPlayerEntity playerEntity, World world, BlockPos pos, GameMode gameMode) {
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt((ServerWorld) world, pos);
        if (claim.isEmpty()) return playerEntity.isBlockBreakingRestricted(world, pos, gameMode);
        if (!claim.get().hasPermission(playerEntity.getUuid(), "mine." + Registry.BLOCK.getId(this.world.getBlockState(pos).getBlock()).getPath())) {
            ClaimPlayer claimPlayer = (ClaimPlayer) playerEntity;
            claimPlayer.sendMessage(Text.translatable("text.itsours.action.disallowed.break_block").formatted(Formatting.RED));
            return true;
        }
        return playerEntity.isBlockBreakingRestricted(world, pos, gameMode);
    }

    @Redirect(
            method = "interactBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"
            )
    )
    private ActionResult canInteractBlock(BlockState blockState, World world, PlayerEntity playerEntity, Hand hand, BlockHitResult hit) {
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt((ServerWorld) world, hit.getBlockPos());
        if (claim.isEmpty() || !PermissionList.filter(blockState.getBlock(), PermissionList.INTERACT_BLOCK))
            return blockState.onUse(world, playerEntity, hand, hit);
        if (!claim.get().hasPermission(playerEntity.getUuid(), "interact_block." + Registry.BLOCK.getId(blockState.getBlock()).getPath())) {
            ClaimPlayer claimPlayer = (ClaimPlayer) playerEntity;
            claimPlayer.sendMessage(Text.translatable("text.itsours.action.disallowed.interact_block").formatted(Formatting.RED));
            return ActionResult.FAIL;
        }
        return blockState.onUse(world, playerEntity, hand, hit);
    }

    @Redirect(
            method = "interactBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"
            )
    )
    private ActionResult canUseOnBlock(ItemStack itemStack, ItemUsageContext context) {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt(context);
        if (claim.isEmpty() || !PermissionList.filter(itemStack.getItem(), PermissionList.USE_ON_BLOCK))
            return itemStack.useOnBlock(context);
        if (!claim.get().hasPermission(player.getUuid(), "use_on_block." + Registry.ITEM.getId(itemStack.getItem()).getPath())) {
            claimPlayer.sendMessage(Text.translatable("text.itsours.action.disallowed.interact_item_on_block").formatted(Formatting.RED));
            return ActionResult.FAIL;
        }
        return itemStack.useOnBlock(context);
    }

    /*@Inject(
            method = "interactBlock",
            at = @At("HEAD")
    )
    private void acquireLocale(ServerPlayerEntity serverPlayerEntity, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        pos = hitResult.getBlockPos().offset(hitResult.getSide());
    }*/


    /*@Redirect(
            method = "interactItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"
            )
    )
    private TypedActionResult<ItemStack> canUseItem(ItemStack itemStack, World world, PlayerEntity user, Hand hand) {
        Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) world, user.getBlockPos());
        if (claim.isEmpty() || !PermissionList.filter(itemStack.getItem(), PermissionList.useItem))
            return itemStack.use(world, user, hand);
        if (!claim.get().hasPermission(user.getUuid(), "use_item." + Registry.ITEM.getId(itemStack.getItem()).getPath())) {
            ClaimPlayer claimPlayer = (ClaimPlayer) user;
            claimPlayer.sendError(Component.text("You can't use that item here.").color(Color.RED));
            return TypedActionResult.fail(itemStack);
        }
        return itemStack.use(world, user, hand);
    }*/
}
