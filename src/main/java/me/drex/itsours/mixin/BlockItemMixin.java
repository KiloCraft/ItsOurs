package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
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
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {

    public BlockItemMixin(Settings settings) {
        super(settings);
    }

    @Shadow
    public abstract Block getBlock();

    @Redirect(
            method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemPlacementContext;canPlace()Z"
            )
    )
    private boolean canPlace(ItemPlacementContext context) {
        if (context.getPlayer() == null) return context.canPlace();
        Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) context.getWorld(), context.getBlockPos());
        if (claim.isEmpty()) return context.canPlace();
        if (!claim.get().hasPermission(context.getPlayer().getUuid(), "place." + Registry.BLOCK.getId(this.getBlock()).getPath())) {
            ClaimPlayer claimPlayer = (ClaimPlayer) context.getPlayer();
            claimPlayer.sendError(Component.text("You can't place a block here.").color(Color.RED));
            return false;
        }
        return context.canPlace();
    }


    @Inject(
            method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V"
            )
    )
    private void onBlockPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (context.getPlayer() == null) return;
        Block block = this.getBlock();
        BlockPos blockPos = context.getBlockPos();
        Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) context.getWorld(), blockPos);
        if ((block instanceof BlockWithEntity || block == Blocks.CRAFTING_TABLE) && !claim.isPresent()) {
            PlayerEntity playerEntity = context.getPlayer();
            ClaimPlayer claimPlayer = (ClaimPlayer) playerEntity;
            if (claimPlayer != null && ItsOursMod.INSTANCE.getClaimList().get(playerEntity.getUuid()).isEmpty()) {
                TextComponent.Builder textComponent = Component.text().content("This " + this.getDefaultStack().getName().getString().toLowerCase() + " is not protected,").color(Color.YELLOW).append(Component.text(" click this ").color(Color.ORANGE)).append(Component.text("message to create a claim, to protect it").color(Color.YELLOW));
                textComponent.clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/claim create"));
                claimPlayer.sendMessage(textComponent.build());
                claimPlayer.setRightPosition(new BlockPos(blockPos.getX() + 3, blockPos.getY(), blockPos.getZ() + 3));
                claimPlayer.setLeftPosition(new BlockPos(blockPos.getX() - 3, blockPos.getY(), blockPos.getZ() - 3));
            }
        }
    }

}
