package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.user.ClaimSelectingPlayer;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static me.drex.itsours.util.LocalizedMessage.localized;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {

    public BlockItemMixin(Properties settings) {
        super(settings);
    }

    @Shadow
    public abstract Block getBlock();

    @ModifyExpressionValue(
        method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/context/BlockPlaceContext;canPlace()Z"
        )
    )
    private boolean itsours$canPlace(boolean original, BlockPlaceContext context) {
        if (context.getPlayer() == null) return original;
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(context);
        if (claim.isEmpty()) return original;
        if (!claim.get().checkAction(context.getPlayer().getUUID(), Flags.PLACE, Node.registry(BuiltInRegistries.BLOCK, this.getBlock()))) {
            context.getPlayer().displayClientMessage(localized("text.itsours.action.disallowed.place_block", context.getPlayer()), true);
            return false;
        }
        return original;
    }


    @Inject(
        method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/Block;setPlacedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V"
        )
    )
    private void itsours$onBlockPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (context.getPlayer() == null) return;
        Block block = this.getBlock();
        BlockPos blockPos = context.getClickedPos();
        Optional<AbstractClaim> optionalClaim = ClaimList.getClaimAt(context);
        // TODO: Make configurable
        if ((block instanceof ChestBlock) && optionalClaim.isEmpty()) {
            Player player = context.getPlayer();
            ClaimSelectingPlayer claimSelectingPlayer = (ClaimSelectingPlayer) player;
            if (claimSelectingPlayer != null && ClaimList.getClaimsFrom(player.getUUID()).isEmpty()) {
                player.displayClientMessage(localized("text.itsours.info.notProtected", player), true);
                claimSelectingPlayer.setFirstPosition(new BlockPos(blockPos.getX() + 3, blockPos.getY(), blockPos.getZ() + 3));
                claimSelectingPlayer.setSecondPosition(new BlockPos(blockPos.getX() - 3, blockPos.getY(), blockPos.getZ() - 3));
            }
        }
    }

}
