package me.drex.itsours.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.util.Modify;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.user.ClaimSelectingPlayer;
import me.drex.itsours.user.ClaimTrackingPlayer;
import me.drex.itsours.util.ClaimBox;
import me.drex.itsours.util.Constants;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static me.drex.itsours.util.LocalizedMessage.localized;
import static net.minecraft.commands.Commands.argument;

public class CreateCommand extends AbstractCommand {

    public static final CommandSyntaxException SELECT_FIRST = new SimpleCommandExceptionType(localized("text.itsours.commands.create.selectFirst")).create();
    public static final DynamicCommandExceptionType INTERSECTS = new DynamicCommandExceptionType(name -> localized("text.itsours.commands.create.intersects", Map.of("input", Component.literal(name.toString()))));
    public static final DynamicCommandExceptionType LIMIT = new DynamicCommandExceptionType(name -> localized("text.itsours.commands.create.limit", Map.of("input", Component.literal(name.toString()))));
    public static final CreateCommand INSTANCE = new CreateCommand();
    public static final String LITERAL = "create";
    private static final int MAX_CHECK = 100;

    private CreateCommand() {
        super(LITERAL);
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        literal.then(
            argument("name", StringArgumentType.word())
                .executes(ctx -> executeCreate(ctx.getSource(), StringArgumentType.getString(ctx, "name")))
                .then(
                    argument("from", BlockPosArgument.blockPos()).then(
                        argument("to", BlockPosArgument.blockPos()).executes(ctx -> executeCreate(
                            ctx.getSource(),
                            StringArgumentType.getString(ctx, "name"),
                            ClaimBox.create(BlockPosArgument.getBlockPos(ctx, "from"), BlockPosArgument.getBlockPos(ctx, "to")))
                        )
                    )
                )
        ).executes(ctx -> executeCreate(ctx.getSource(), ctx.getSource().getTextName()));
    }

    private int executeCreate(CommandSourceStack src, String claimName, ClaimBox claimBox) throws CommandSyntaxException {
        int limit = Constants.DEFAULT_CLAIM_COUNT;
        if (ItsOurs.checkPermission(src, "itsours.max.bypass", 2)) {
            limit = Integer.MAX_VALUE;
        } else {
            for (int i = 0; i < MAX_CHECK; i++) {
                if (ItsOurs.checkPermission(src, "itsours." + ("max." + i), 2)) {
                    limit = Math.max(limit, i);
                }
            }
        }

        ServerPlayer player = src.getPlayer();
        UUID uuid = player.getUUID();
        if (AbstractClaim.isNameInvalid(claimName)) throw ClaimArgument.INVALID_NAME;
        Optional<AbstractClaim> optional = ClaimList.getClaims().stream().filter((claim) ->
            claim.getDimension().equals(player.level().dimension()) &&
                claimBox.intersects(claim.getBox())
        ).max(Comparator.comparingInt(AbstractClaim::getDepth));
        if (optional.isPresent()) {
            AbstractClaim intersectingClaim = optional.get();
            if (intersectingClaim.getBox().contains(claimBox)) {
                return createSubzone(src, claimName, intersectingClaim, claimBox);
            } else {
                throw INTERSECTS.create(intersectingClaim.getFullName());
            }
        }
        if (ClaimList.getClaimsFrom(src.getPlayer().getUUID()).size() >= limit) {
            throw LIMIT.create(limit);
        }
        // Main claim
        Claim claim = new Claim(claimName, uuid, claimBox, src.getLevel());
        long requiredBlocks = claimBox.getArea();
        // Check and remove claim blocks
        long blocks = DataManager.getUserData(uuid).blocks();
        if (requiredBlocks > blocks) {
            src.sendFailure(localized("text.itsours.commands.expand.missingClaimBlocks", Map.of("blocks", Component.literal(String.valueOf(requiredBlocks - blocks)))));
            return 0;
        }
        if (ClaimList.getClaim(claimName).isPresent()) throw ClaimArgument.NAME_TAKEN;
        DataManager.updateUserData(uuid).setBlocks(blocks - requiredBlocks);
        ClaimList.addClaim(claim);
        src.sendSuccess(() -> localized("text.itsours.commands.create", claim.placeholders(src.getServer())), false);
        claim.notifyTrackingChanges(src.getServer(), true);
        // reset positions
        return 1;
    }

    private int executeCreate(CommandSourceStack src, String claimName) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayer();
        ClaimSelectingPlayer claimSelectingPlayer = (ClaimSelectingPlayer) player;
        if (!claimSelectingPlayer.arePositionsSet()) throw SELECT_FIRST;
        ClaimBox selectedBox = ClaimBox.create(claimSelectingPlayer.getFirstPosition().atY(src.getLevel().getMinY()), claimSelectingPlayer.getSecondPosition().atY(src.getLevel().getMaxY()));
        int result = executeCreate(src, claimName, selectedBox);
        claimSelectingPlayer.resetSelection();
        ((ClaimTrackingPlayer) player).trackClaims();
        return result;
    }

    private int createSubzone(CommandSourceStack src, String claimName, AbstractClaim parent, ClaimBox claimBox) throws CommandSyntaxException {
        // Subzone
        ServerPlayer player = src.getPlayerOrException();
        for (Subzone subzone : parent.getSubzones()) {
            if (subzone.getBox().intersects(claimBox)) throw INTERSECTS.create(subzone.getFullName());
            if (subzone.getName().equals(claimName)) throw ClaimArgument.NAME_TAKEN;
        }
        validateAction(src, parent, Flags.MODIFY, Modify.SUBZONE.node());
        Subzone subzone = new Subzone(claimName, claimBox, player.level(), parent);
        ClaimList.addClaim(subzone);
        subzone.notifyTrackingChanges(src.getServer(), true);
        ((ClaimSelectingPlayer) player).resetSelection();
        ((ClaimTrackingPlayer) player).trackClaims();
        return 1;
    }

}
