package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.util.Permission;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {

    public BlockItemMixin(Settings settings) {
        super(settings);
    }

    @Shadow
    public abstract Block getBlock();

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "HEAD"))
    private void ItsOurs$checkPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
    }

    @Redirect(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemPlacementContext;canPlace()Z"))
    private boolean ItsOurs$checkCanPlace(ItemPlacementContext context) {
        if (context.getPlayer() == null) return context.canPlace();
        AbstractClaim claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) context.getWorld(), context.getBlockPos());
        if (claim == null) return context.canPlace();
        if (!claim.hasPermission(context.getPlayer().getUuid(), "place." + Permission.toString(this.getBlock()))) {
            ClaimPlayer claimPlayer = (ClaimPlayer) context.getPlayer();
            claimPlayer.sendError(Component.text("You can't place a block here.").color(Color.RED));
            return false;
        }
        return context.canPlace();
    }


    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V"))
    private void ItsOurs$placeBlock(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (context.getPlayer() == null) return;
        Block block = this.getBlock();
        BlockPos blockPos = context.getBlockPos();
        if (block instanceof BlockWithEntity || block == Blocks.CRAFTING_TABLE) {
            PlayerEntity playerEntity = context.getPlayer();
            ClaimPlayer claimPlayer = (ClaimPlayer) playerEntity;
            if (claimPlayer != null) {
                TextComponent.Builder textComponent = Component.text().content("This " + this.getDefaultStack().getName().getString().toLowerCase() + " is not protected,").color(Color.YELLOW).append(Component.text(" click this ").color(Color.ORANGE)).append(Component.text("message to create a claim, to protect it").color(Color.YELLOW));
                textComponent.clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/claim create"));
                claimPlayer.sendMessage(textComponent.build());
                if (!claimPlayer.arePositionsSet()) {
                    claimPlayer.setRightPosition(new BlockPos(blockPos.getX() + 3, blockPos.getY(), blockPos.getZ() + 3));
                    claimPlayer.setLeftPosition(new BlockPos(blockPos.getX() - 3, blockPos.getY(), blockPos.getZ() - 3));
                }
            }
        }
    }

}
