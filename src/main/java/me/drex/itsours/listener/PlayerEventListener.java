package me.drex.itsours.listener;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Constants;
import me.drex.itsours.util.PlaceholderUtil;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.Optional;

import static me.drex.message.api.LocalizedMessage.localized;

public class PlayerEventListener {

    public static void registerPlayerListeners() {
        UseBlockCallback.EVENT.register(PlayerEventListener::onBlockUse);
        AttackBlockCallback.EVENT.register(PlayerEventListener::onBlockAttack);
        UseItemCallback.EVENT.register(PlayerEventListener::onInteractItem);
    }

    private static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        final BlockPos pos = hitResult.getBlockPos();
        if (shouldSelect(player, hand, claimPlayer.getSecondPosition(), pos)) {
            player.sendMessage(localized("text.itsours.select.pos2", PlaceholderUtil.vec3i("pos_", pos)));
            claimPlayer.setSecondPosition(pos);
            onSelectCorner(player);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    private static ActionResult onBlockAttack(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        if (shouldSelect(player, hand, claimPlayer.getFirstPosition(), pos)) {
            player.sendMessage(localized("text.itsours.select.pos1", PlaceholderUtil.vec3i("pos_", pos)));
            claimPlayer.setFirstPosition(pos);
            onSelectCorner(player);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    private static TypedActionResult<ItemStack> onInteractItem(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(player);
        if (claim.isEmpty() || !PermissionManager.USE_ITEM_PREDICATE.test(stack.getItem()))
            return TypedActionResult.pass(stack);
        if (!claim.get().hasPermission(player.getUuid(), PermissionManager.USE_ITEM, Node.registry(Registries.ITEM, stack.getItem()))) {
            player.sendMessage(localized("text.itsours.action.disallowed.interact_item"), true);
            return TypedActionResult.fail(stack);
        }
        return TypedActionResult.pass(stack);
    }

    private static void onSelectCorner(PlayerEntity player) {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        if (claimPlayer.arePositionsSet()) {
            if (ClaimList.getClaimsFrom(player.getUuid()).isEmpty()) {
                player.sendMessage(localized("text.itsours.select.done.first"));
            } else {
                player.sendMessage(localized("text.itsours.select.done"));
            }
        }
    }

    private static boolean shouldSelect(PlayerEntity player, Hand hand, BlockPos prevPos, BlockPos pos) {
        if (player.getStackInHand(hand).isOf(Constants.SELECT_ITEM) || DataManager.getUserData(player.getUuid()).select()) {
            return !Objects.equals(prevPos, pos);
        }
        return false;
    }
}
