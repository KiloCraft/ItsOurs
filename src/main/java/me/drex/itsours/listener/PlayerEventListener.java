package me.drex.itsours.listener;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.flags.util.FlagBuilderUtil;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.user.ClaimSelectingPlayer;
import me.drex.itsours.user.ClaimTrackingPlayer;
import me.drex.itsours.util.ClaimBox;
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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

import static me.drex.message.api.LocalizedMessage.localized;

public class PlayerEventListener {

    public static void registerPlayerListeners() {
        UseBlockCallback.EVENT.register(PlayerEventListener::onBlockUse);
        AttackBlockCallback.EVENT.register(PlayerEventListener::onBlockAttack);
        UseItemCallback.EVENT.register(PlayerEventListener::onInteractItem);
    }

    private static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        ClaimSelectingPlayer claimSelectingPlayer = (ClaimSelectingPlayer) player;
        final BlockPos pos = hitResult.getBlockPos();
        if (shouldSelect(player, hand)) {
            player.sendMessage(localized("text.itsours.select.pos2", PlaceholderUtil.vec3i("pos_", pos)), false);
            claimSelectingPlayer.setSecondPosition(pos);
            onSelectCorner(player);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    private static ActionResult onBlockAttack(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        ClaimSelectingPlayer claimSelectingPlayer = (ClaimSelectingPlayer) player;
        if (shouldSelect(player, hand)) {
            player.sendMessage(localized("text.itsours.select.pos1", PlaceholderUtil.vec3i("pos_", pos)), false);
            claimSelectingPlayer.setFirstPosition(pos);
            onSelectCorner(player);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    private static ActionResult onInteractItem(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(player);
        if (claim.isEmpty() || !FlagBuilderUtil.USE_ITEM_PREDICATE.test(stack.getItem()))
            return ActionResult.PASS;
        if (!claim.get().checkAction(player.getUuid(), Flags.USE_ITEM, Node.registry(Registries.ITEM, stack.getItem()))) {
            player.sendMessage(localized("text.itsours.action.disallowed.interact_item"), true);
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }

    private static void onSelectCorner(PlayerEntity player) {
        ClaimSelectingPlayer claimSelectingPlayer = (ClaimSelectingPlayer) player;
        ClaimTrackingPlayer claimTrackingPlayer = (ClaimTrackingPlayer)player;
        claimTrackingPlayer.trackClaims();
        if (claimSelectingPlayer.arePositionsSet()) {
            AbstractClaim previous = claimSelectingPlayer.claim();
            if (previous != null) {
                previous.notifyTrackingChanges(player.getServer(), false);
            }
            ClaimBox selectedBox = ClaimBox.create(claimSelectingPlayer.getFirstPosition().withY(player.getWorld().getBottomY()), claimSelectingPlayer.getSecondPosition().withY(player.getWorld().getTopYInclusive()));

            Claim claim = new Claim("", player.getUuid(), selectedBox, player.getWorld());
            claim.notifyTrackingChanges(player.getServer(), true);
            claimSelectingPlayer.setClaim(claim);

            if (ClaimList.getClaimsFrom(player.getUuid()).isEmpty()) {
                player.sendMessage(localized("text.itsours.select.done.first"), false);
            } else {
                player.sendMessage(localized("text.itsours.select.done"), false);
            }
        }
    }

    private static boolean shouldSelect(PlayerEntity player, Hand hand) {
        return player.getStackInHand(hand).isOf(Constants.SELECT_ITEM) || DataManager.getUserData(player.getUuid()).select();
    }
}
