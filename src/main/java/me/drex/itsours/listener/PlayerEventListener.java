package me.drex.itsours.listener;

import me.drex.itsours.ItsOurs;
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
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Optional;

import static me.drex.message.api.LocalizedMessage.localized;

public class PlayerEventListener {

    public static final Identifier BEFORE_DEFAULT = Identifier.of(ItsOurs.MOD_ID, "before_default");

    public static void registerPlayerListeners() {
        UseBlockCallback.EVENT.addPhaseOrdering(BEFORE_DEFAULT, Event.DEFAULT_PHASE);
        UseBlockCallback.EVENT.register(BEFORE_DEFAULT, PlayerEventListener::onBlockUse);
        AttackBlockCallback.EVENT.addPhaseOrdering(BEFORE_DEFAULT, Event.DEFAULT_PHASE);
        AttackBlockCallback.EVENT.register(BEFORE_DEFAULT, PlayerEventListener::onBlockAttack);
        UseItemCallback.EVENT.addPhaseOrdering(BEFORE_DEFAULT, Event.DEFAULT_PHASE);
        UseItemCallback.EVENT.register(BEFORE_DEFAULT, PlayerEventListener::onInteractItem);
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
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, pos);
        if (claim.isPresent()) {
            BlockState state = world.getBlockState(pos);
            if (!claim.get().checkAction(player.getUuid(), Flags.MINE, Node.registry(Registries.BLOCK, state.getBlock()))) {
                player.sendMessage(localized("text.itsours.action.disallowed.break_block"), true);
                return ActionResult.FAIL;
            }
        }
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
                previous.notifyTrackingChanges(player.getEntityWorld().getServer(), false);
            }
            ClaimBox selectedBox = ClaimBox.create(claimSelectingPlayer.getFirstPosition().withY(player.getEntityWorld().getBottomY()), claimSelectingPlayer.getSecondPosition().withY(player.getEntityWorld().getTopYInclusive()));

            Claim claim = new Claim("", player.getUuid(), selectedBox, player.getEntityWorld());
            claim.notifyTrackingChanges(player.getEntityWorld().getServer(), true);
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
