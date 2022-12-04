package me.drex.itsours.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.command.CommandManager;
import me.drex.itsours.command.CreateCommand;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {

    public BlockItemMixin(Settings settings) {
        super(settings);
    }

    @Shadow
    public abstract Block getBlock();

    @ModifyExpressionValue(
            method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemPlacementContext;canPlace()Z"
            )
    )
    private boolean itsours$canPlace(boolean original, ItemPlacementContext context) {
        if (context.getPlayer() == null) return original;
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt(context);
        if (claim.isEmpty()) return original;
        if (!claim.get().hasPermission(context.getPlayer().getUuid(), PermissionManager.PLACE, Node.dummy(Registries.BLOCK, this.getBlock()))) {
            context.getPlayer().sendMessage(Text.translatable("text.itsours.action.disallowed.place_block").formatted(Formatting.RED), true);
            return false;
        }
        return original;
    }


    @Inject(
            method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V"
            )
    )
    private void itsours$onBlockPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (context.getPlayer() == null) return;
        Block block = this.getBlock();
        BlockPos blockPos = context.getBlockPos();
        Optional<AbstractClaim> optional = ClaimList.INSTANCE.getClaimAt(context);
        if ((block instanceof BlockWithEntity || block == Blocks.CRAFTING_TABLE) && optional.isEmpty()) {
            PlayerEntity playerEntity = context.getPlayer();
            ClaimPlayer claimPlayer = (ClaimPlayer) playerEntity;
            if (claimPlayer != null && ClaimList.INSTANCE.getClaimsFrom(playerEntity.getUuid()).isEmpty()) {
                playerEntity.sendMessage(Text.translatable("text.itsours.info.notProtected",
                        Text.literal(this.getDefaultStack().getName().getString().toLowerCase()).formatted(Formatting.GOLD)
                ).formatted(Formatting.YELLOW).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s", CommandManager.LITERAL, CreateCommand.LITERAL)))));
                claimPlayer.setFirstPosition(new BlockPos(blockPos.getX() + 3, blockPos.getY(), blockPos.getZ() + 3));
                claimPlayer.setSecondPosition(new BlockPos(blockPos.getX() - 3, blockPos.getY(), blockPos.getZ() - 3));
            }
        }
    }

}
