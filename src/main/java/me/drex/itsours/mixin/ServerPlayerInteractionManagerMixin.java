package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import me.drex.itsours.util.TextComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

import java.awt.*;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "interactBlock", at = @At("HEAD"))
    private void onInteract(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        BlockPos pos = hitResult.getBlockPos();
        if (stack.getItem() == Items.GOLDEN_SHOVEL && !isSame(claimPlayer.getRightPosition(), pos)) {
            claimPlayer.sendMessage(Component.text("Position #2 set to " + pos.getX() + " " + pos.getZ()).color(Color.LIGHT_GREEN));
            claimPlayer.setRightPosition(pos);
            onClaimAddCorner();
        }
        //TODO: Make sure the chest was actually placed
//        if (stack.getItem() == Items.CHEST && ItsOursMod.INSTANCE.getClaimList().get(player.getUuid()).isEmpty()) {
//            claimPlayer.sendMessage(Component.text("This " + stack.getName().getString().toLowerCase() + " is not protected, click this message to create a claim, to protect it").color(Color.ORANGE).clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/claim create")));
//            BlockPos blockPos = hitResult.getBlockPos().offset(hitResult.getSide());
//            if (!claimPlayer.arePositionsSet()) {
//                claimPlayer.setRightPosition(new BlockPos(blockPos.getX() + 3, blockPos.getY(), blockPos.getZ() + 3));
//                claimPlayer.setLeftPosition(new BlockPos(blockPos.getX() - 3, blockPos.getY(), blockPos.getZ() - 3));
//            }
//        }
    }

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"))
    private void onMine(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, CallbackInfo ci) {
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


}
