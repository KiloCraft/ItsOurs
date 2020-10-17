package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.util.Group;
import me.drex.itsours.claim.permission.util.Permission;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow public ServerWorld world;

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void ItsOurs$onRightClickOnBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        BlockPos pos = hitResult.getBlockPos();
        if (stack.getItem() == Items.GOLDEN_SHOVEL && !isSame(claimPlayer.getRightPosition(), pos)) {
            claimPlayer.sendMessage(Component.text("Position #2 set to " + pos.getX() + " " + pos.getZ()).color(Color.LIGHT_GREEN));
            claimPlayer.setRightPosition(pos);
            onClaimAddCorner();
        }
    }

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"))
    private void ItsOurs$onLeftClickOnBlock(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, CallbackInfo ci) {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        if (player.inventory.getMainHandStack().getItem() == Items.GOLDEN_SHOVEL && !isSame(claimPlayer.getLeftPosition(), pos)) {
            claimPlayer.sendMessage(Component.text("Position #1 set to " + pos.getX() + " " + pos.getZ()).color(Color.LIGHT_GREEN));
            claimPlayer.setLeftPosition(pos);
            onClaimAddCorner();
        }
    }



    public void onClaimAddCorner() {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        if (claimPlayer.arePositionsSet()) {
            TextComponent.Builder builder =  Component.text().content("Area Selected. Click to create your claim!").color(Color.ORANGE);
            if (ItsOursMod.INSTANCE.getClaimList().get(player.getUuid()).isEmpty()) {
                builder.clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/claim create " + player.getEntityName()));
            } else {
                builder.clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand("/claim create name"));
            }
            claimPlayer.sendMessage(builder.build());
        }
    }

    private boolean isSame(BlockPos pos1, BlockPos pos2) {
        return pos1 != null && pos2 != null && pos1.getX() == pos2.getX() && pos1.getY() == pos2.getY() && pos1.getZ() == pos2.getZ();
    }

    @Redirect(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isBlockBreakingRestricted(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/GameMode;)Z"))
    private boolean ItsOurs$onBlockBreak(ServerPlayerEntity playerEntity, World world, BlockPos pos, GameMode gameMode) {
        AbstractClaim claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) world, pos);
        if (claim == null) return playerEntity.isBlockBreakingRestricted(world, pos, gameMode);
        if (!claim.hasPermission(playerEntity.getUuid(), "mine." + Permission.toString(this.world.getBlockState(pos).getBlock()))) {
            ClaimPlayer claimPlayer = (ClaimPlayer) playerEntity;
            claimPlayer.sendError(Component.text("You can't break that block here.").color(Color.RED));
            return true;
        }
        return playerEntity.isBlockBreakingRestricted(world, pos, gameMode);
    }

    @Redirect(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult ItsOurs$onBlockInteract(BlockState blockState, World world, PlayerEntity playerEntity, Hand hand, BlockHitResult hit) {
        AbstractClaim claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) world, hit.getBlockPos());
        if (claim == null || !Group.filter(blockState.getBlock(), Group.INTERACT_FILTER)) return blockState.onUse(world, playerEntity, hand, hit);
        if (!claim.hasPermission(playerEntity.getUuid(), "interact_block." + Permission.toString(blockState.getBlock()))) {
            ClaimPlayer claimPlayer = (ClaimPlayer) playerEntity;
            claimPlayer.sendError(Component.text("You can't interact with that block here.").color(Color.RED));
            return ActionResult.FAIL;
        }
        return blockState.onUse(world, playerEntity, hand, hit);
    }


}
