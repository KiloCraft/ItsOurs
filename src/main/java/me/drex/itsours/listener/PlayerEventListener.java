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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.Optional;

import static me.drex.itsours.util.LocalizedMessage.localized;
import static me.drex.itsours.util.LocalizedMessage.sendMessage;

public class PlayerEventListener {

    public static final Identifier BEFORE_DEFAULT = Identifier.fromNamespaceAndPath(ItsOurs.MOD_ID, "before_default");

    public static void registerPlayerListeners() {
        UseBlockCallback.EVENT.addPhaseOrdering(BEFORE_DEFAULT, Event.DEFAULT_PHASE);
        UseBlockCallback.EVENT.register(BEFORE_DEFAULT, PlayerEventListener::onBlockUse);
        AttackBlockCallback.EVENT.addPhaseOrdering(BEFORE_DEFAULT, Event.DEFAULT_PHASE);
        AttackBlockCallback.EVENT.register(BEFORE_DEFAULT, PlayerEventListener::onBlockAttack);
        UseItemCallback.EVENT.addPhaseOrdering(BEFORE_DEFAULT, Event.DEFAULT_PHASE);
        UseItemCallback.EVENT.register(BEFORE_DEFAULT, PlayerEventListener::onInteractItem);
    }

    private static InteractionResult onBlockUse(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        ClaimSelectingPlayer claimSelectingPlayer = (ClaimSelectingPlayer) player;
        final BlockPos pos = hitResult.getBlockPos();
        if (shouldSelect(player, hand)) {
            sendMessage(player, localized("text.itsours.select.pos2", PlaceholderUtil.vec3i("pos_", pos), player), false);
            claimSelectingPlayer.setSecondPosition(pos);
            onSelectCorner(player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult onBlockAttack(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        ClaimSelectingPlayer claimSelectingPlayer = (ClaimSelectingPlayer) player;
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, pos);
        if (claim.isPresent()) {
            BlockState state = world.getBlockState(pos);
            if (!claim.get().checkAction(player.getUUID(), Flags.MINE, Node.registry(BuiltInRegistries.BLOCK, state.getBlock()))) {
                sendMessage(player, localized("text.itsours.action.disallowed.break_block", player), true);
                return InteractionResult.FAIL;
            }
        }
        if (shouldSelect(player, hand)) {
            sendMessage(player, localized("text.itsours.select.pos1", PlaceholderUtil.vec3i("pos_", pos), player), false);
            claimSelectingPlayer.setFirstPosition(pos);
            onSelectCorner(player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult onInteractItem(Player player, Level world, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(player);
        if (claim.isEmpty() || !FlagBuilderUtil.USE_ITEM_PREDICATE.test(stack.getItem()))
            return InteractionResult.PASS;
        if (!claim.get().checkAction(player.getUUID(), Flags.USE_ITEM, Node.registry(BuiltInRegistries.ITEM, stack.getItem()))) {
            sendMessage(player, localized("text.itsours.action.disallowed.interact_item", player), true);
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    private static void onSelectCorner(Player player) {
        ClaimSelectingPlayer claimSelectingPlayer = (ClaimSelectingPlayer) player;
        ClaimTrackingPlayer claimTrackingPlayer = (ClaimTrackingPlayer)player;
        claimTrackingPlayer.trackClaims();
        if (claimSelectingPlayer.arePositionsSet()) {
            AbstractClaim previous = claimSelectingPlayer.claim();
            if (previous != null) {
                previous.notifyTrackingChanges(player.level().getServer(), false);
            }
            ClaimBox selectedBox = ClaimBox.create(claimSelectingPlayer.getFirstPosition().atY(player.level().getMinY()), claimSelectingPlayer.getSecondPosition().atY(player.level().getMaxY()));

            Claim claim = new Claim("", player.getUUID(), selectedBox, player.level());
            claim.notifyTrackingChanges(player.level().getServer(), true);
            claimSelectingPlayer.setClaim(claim);

            if (ClaimList.getClaimsFrom(player.getUUID()).isEmpty()) {
                sendMessage(player, localized("text.itsours.select.done.first", player), false);
            } else {
                sendMessage(player, localized("text.itsours.select.done", player), false);
            }
        }
    }

    private static boolean shouldSelect(Player player, InteractionHand hand) {
        return player.getItemInHand(hand).is(Constants.SELECT_ITEM) || DataManager.getUserData(player.getUUID()).select();
    }
}
