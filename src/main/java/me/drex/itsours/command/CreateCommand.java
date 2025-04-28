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
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static me.drex.message.api.LocalizedMessage.localized;
import static net.minecraft.server.command.CommandManager.argument;

public class CreateCommand extends AbstractCommand {

    public static final CommandSyntaxException SELECT_FIRST = new SimpleCommandExceptionType(localized("text.itsours.commands.create.selectFirst")).create();
    public static final DynamicCommandExceptionType INTERSECTS = new DynamicCommandExceptionType(name -> localized("text.itsours.commands.create.intersects", Map.of("input", Text.literal(name.toString()))));
    public static final DynamicCommandExceptionType LIMIT = new DynamicCommandExceptionType(name -> localized("text.itsours.commands.create.limit", Map.of("input", Text.literal(name.toString()))));
    public static final CreateCommand INSTANCE = new CreateCommand();
    public static final String LITERAL = "create";
    private static final int MAX_CHECK = 100;

    private CreateCommand() {
        super(LITERAL);
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(
            argument("name", StringArgumentType.word())
                .executes(ctx -> executeCreate(ctx.getSource(), StringArgumentType.getString(ctx, "name")))
                .then(
                    argument("from", BlockPosArgumentType.blockPos()).then(
                        argument("to", BlockPosArgumentType.blockPos()).executes(ctx -> executeCreate(
                            ctx.getSource(),
                            StringArgumentType.getString(ctx, "name"),
                            ClaimBox.create(BlockPosArgumentType.getBlockPos(ctx, "from"), BlockPosArgumentType.getBlockPos(ctx, "to")))
                        )
                    )
                )
        ).executes(ctx -> executeCreate(ctx.getSource(), ctx.getSource().getName()));
    }

    private int executeCreate(ServerCommandSource src, String claimName, ClaimBox claimBox) throws CommandSyntaxException {
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

        ServerPlayerEntity player = src.getPlayer();
        UUID uuid = player.getUuid();
        if (AbstractClaim.isNameInvalid(claimName)) throw ClaimArgument.INVALID_NAME;
        Optional<AbstractClaim> optional = ClaimList.getClaims().stream().filter((claim) ->
            claim.getDimension().equals(player.getWorld().getRegistryKey()) &&
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
        if (ClaimList.getClaimsFrom(src.getPlayer().getUuid()).size() >= limit) {
            throw LIMIT.create(limit);
        }
        // Main claim
        Claim claim = new Claim(claimName, uuid, claimBox, src.getWorld());
        long requiredBlocks = claimBox.getArea();
        // Check and remove claim blocks
        long blocks = DataManager.getUserData(uuid).blocks();
        if (requiredBlocks > blocks) {
            src.sendError(localized("text.itsours.commands.expand.missingClaimBlocks", Map.of("blocks", Text.literal(String.valueOf(requiredBlocks - blocks)))));
            return 0;
        }
        if (ClaimList.getClaim(claimName).isPresent()) throw ClaimArgument.NAME_TAKEN;
        DataManager.updateUserData(uuid).setBlocks(blocks - requiredBlocks);
        ClaimList.addClaim(claim);
        src.sendFeedback(() -> localized("text.itsours.commands.create", claim.placeholders(src.getServer())), false);
        claim.notifyTrackingChanges(src.getServer(), true);
        // reset positions
        return 1;
    }

    private int executeCreate(ServerCommandSource src, String claimName) throws CommandSyntaxException {
        ServerPlayerEntity player = src.getPlayer();
        ClaimSelectingPlayer claimSelectingPlayer = (ClaimSelectingPlayer) player;
        if (!claimSelectingPlayer.arePositionsSet()) throw SELECT_FIRST;
        ClaimBox selectedBox = ClaimBox.create(claimSelectingPlayer.getFirstPosition().withY(src.getWorld().getBottomY()), claimSelectingPlayer.getSecondPosition().withY(src.getWorld().getTopYInclusive()));
        int result = executeCreate(src, claimName, selectedBox);
        claimSelectingPlayer.resetSelection();
        ((ClaimTrackingPlayer) player).trackClaims();
        return result;
    }

    private int createSubzone(ServerCommandSource src, String claimName, AbstractClaim parent, ClaimBox claimBox) throws CommandSyntaxException {
        // Subzone
        ServerPlayerEntity player = src.getPlayerOrThrow();
        for (Subzone subzone : parent.getSubzones()) {
            if (subzone.getBox().intersects(claimBox)) throw INTERSECTS.create(subzone.getFullName());
            if (subzone.getName().equals(claimName)) throw ClaimArgument.NAME_TAKEN;
        }
        validateAction(src, parent, Flags.MODIFY, Modify.SUBZONE.node());
        Subzone subzone = new Subzone(claimName, claimBox, player.getServerWorld(), parent);
        ClaimList.addClaim(subzone);
        subzone.notifyTrackingChanges(src.getServer(), true);
        ((ClaimSelectingPlayer) player).resetSelection();
        ((ClaimTrackingPlayer) player).trackClaims();
        return 1;
    }

}
