package me.drex.itsours.listener;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.command.CommandManager;
import me.drex.itsours.command.CreateCommand;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.Optional;

public class PlayerEventListener {

    // TODO: Config
    private static final Item SELECT_ITEM = Items.GOLDEN_SHOVEL;

    public static void registerPlayerListeners() {
        // TODO: Gamemode checks
        UseBlockCallback.EVENT.register(PlayerEventListener::onBlockUse);
        AttackBlockCallback.EVENT.register(PlayerEventListener::onBlockAttack);
        UseItemCallback.EVENT.register(PlayerEventListener::onInteractItem);
    }

    private static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        final BlockPos pos = hitResult.getBlockPos();
        if (shouldSelect(player, hand, claimPlayer.getSecondPosition(), pos)) {
            player.sendMessage(Text.translatable("text.itsours.select.pos2", pos.getX(), pos.getY(), pos.getZ()).formatted(Formatting.GREEN));
            claimPlayer.setSecondPosition(pos);
            onSelectCorner(player);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    private static ActionResult onBlockAttack(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        if (shouldSelect(player, hand, claimPlayer.getFirstPosition(), pos)) {
            player.sendMessage(Text.translatable("text.itsours.select.pos1", pos.getX(), pos.getY(), pos.getZ()).formatted(Formatting.GREEN));
            claimPlayer.setFirstPosition(pos);
            onSelectCorner(player);
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    private static TypedActionResult<ItemStack> onInteractItem(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt(player);
        if (claim.isEmpty() || !PermissionManager.USE_ITEM_PREDICATE.test(stack.getItem()))
            return TypedActionResult.pass(stack);
        if (!claim.get().hasPermission(player.getUuid(), PermissionManager.USE_ITEM, Node.dummy(Registry.ITEM, stack.getItem()))) {
            player.sendMessage(Text.translatable("text.itsours.action.disallowed.interact_item").formatted(Formatting.RED));
            return TypedActionResult.fail(stack);
        }
        return TypedActionResult.pass(stack);
    }

    private static void onSelectCorner(PlayerEntity player) {
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        if (claimPlayer.arePositionsSet()) {
            MutableText text = Text.translatable("text.itsours.select.done").formatted(Formatting.GOLD);
            if (ClaimList.INSTANCE.getClaimsFrom(player.getUuid()).isEmpty()) {
                text.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s %s", CommandManager.LITERAL, CreateCommand.LITERAL, player.getEntityName()))));
            } else {
                text.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, String.format("/%s %s name", CommandManager.LITERAL, CreateCommand.LITERAL))));
            }
            player.sendMessage(text);
        }
    }

    private static boolean shouldSelect(PlayerEntity player, Hand hand, BlockPos prevPos, BlockPos pos) {
        if (player.getStackInHand(hand).isOf(SELECT_ITEM) || PlayerList.get(player.getUuid(), Settings.SELECT)) {
            return !Objects.equals(prevPos, pos);
        }
        return false;
    }
}
