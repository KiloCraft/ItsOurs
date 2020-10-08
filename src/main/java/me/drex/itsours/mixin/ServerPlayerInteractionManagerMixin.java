package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void onInteract(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        BlockPos pos = hitResult.getBlockPos();
        if (stack.getItem() == Items.GOLDEN_SHOVEL && !isSame(claimPlayer.getRightPosition(), pos)) {
            claimPlayer.sendMessage(new LiteralText("Position #2 set to " + pos.getX() + " " + pos.getZ()).formatted(Formatting.GREEN));
            claimPlayer.setRightPosition(pos);
            onClaimAddCorner();
        }
        //TODO: Make sure the chest was actually placed
        if (stack.getItem() == Items.CHEST && ItsOursMod.INSTANCE.getClaimList().get(player.getUuid()).isEmpty()) {
            claimPlayer.sendMessage(new LiteralText("This " + stack.getName().getString().toLowerCase() + " is not protected, click this message to create a claim, to protect it").formatted(Formatting.GOLD)
                    .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/claim create"))));
            BlockPos blockPos = hitResult.getBlockPos().offset(hitResult.getSide());
            if (!claimPlayer.arePositionsSet()) {
                claimPlayer.setRightPosition(new BlockPos(blockPos.getX() + 3, blockPos.getY(), blockPos.getZ() + 3));
                claimPlayer.setLeftPosition(new BlockPos(blockPos.getX() - 3, blockPos.getY(), blockPos.getZ() - 3));
            }
        }
    }

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"))
    private void onMine(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, CallbackInfo ci) {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        if (player.inventory.getMainHandStack().getItem() == Items.GOLDEN_SHOVEL && !isSame(claimPlayer.getLeftPosition(), pos)) {
            claimPlayer.sendMessage(new LiteralText("Position #1 set to " + pos.getX() + " " + pos.getZ()).formatted(Formatting.GREEN));
            claimPlayer.setLeftPosition(pos);
            onClaimAddCorner();
        }
    }

    public void onClaimAddCorner() {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        if (claimPlayer.arePositionsSet()) {
            MutableText text = new LiteralText("Area Selected. Click to create your claim!").formatted(Formatting.GOLD);
            if (ItsOursMod.INSTANCE.getClaimList().get(player.getUuid()).isEmpty()) {
                text.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/claim create " + player.getEntityName())));
            } else {
                text.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/claim create <name>")));
            }
            claimPlayer.sendMessage(text);
        }
    }

    private boolean isSame(BlockPos pos1, BlockPos pos2) {
        return pos1 != null && pos2 != null && pos1.getX() == pos2.getX() && pos1.getY() == pos2.getY() && pos1.getZ() == pos2.getZ();
    }


}
