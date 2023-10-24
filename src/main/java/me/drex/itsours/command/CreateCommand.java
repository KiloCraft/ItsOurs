package me.drex.itsours.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.util.Modify;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.ClaimBox;
import me.drex.itsours.util.Constants;
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
        ).executes(ctx -> executeCreate(ctx.getSource(), ctx.getSource().getName()));
    }

    private int executeCreate(ServerCommandSource src, String claimName) throws CommandSyntaxException {
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
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        if (AbstractClaim.isNameInvalid(claimName)) throw ClaimArgument.INVALID_NAME;
        if (!claimPlayer.arePositionsSet()) throw SELECT_FIRST;
        ClaimBox selectedBox = ClaimBox.create(claimPlayer.getFirstPosition().withY(src.getWorld().getBottomY()), claimPlayer.getSecondPosition().withY(src.getWorld().getTopY() - 1));
        Optional<AbstractClaim> optional = ClaimList.getClaims().stream().filter((claim) ->
            claim.getDimension().equals(player.getWorld().getRegistryKey()) &&
                selectedBox.intersects(claim.getBox())
        ).max(Comparator.comparingInt(AbstractClaim::getDepth));
        if (optional.isPresent()) {
            AbstractClaim intersectingClaim = optional.get();
            if (intersectingClaim.getBox().contains(selectedBox)) {
                return createSubzone(src, claimName, intersectingClaim, selectedBox);
            } else {
                throw INTERSECTS.create(intersectingClaim.getFullName());
            }
        }
        if (ClaimList.getClaimsFrom(src.getPlayer().getUuid()).size() >= limit) {
            throw LIMIT.create(limit);
        }
        // Main claim
        Claim claim = new Claim(claimName, uuid, selectedBox, src.getWorld());
        int requiredBlocks = selectedBox.getArea();
        // Check and remove claim blocks
        int blocks = DataManager.getUserData(uuid).blocks();
        if (requiredBlocks > blocks) {
            src.sendError(localized("text.itsours.commands.expand.missingClaimBlocks", Map.of("blocks", Text.literal(String.valueOf(requiredBlocks - blocks)))));
            return 0;
        }
        if (ClaimList.getClaim(claimName).isPresent()) throw ClaimArgument.NAME_TAKEN;
        DataManager.getUserData(uuid).setBlocks(blocks - requiredBlocks);
        ClaimList.addClaim(claim);
        claimPlayer.setLastShow(claim, src.getPlayer().getBlockPos(), src.getWorld());
        claim.show(player, true);
        // reset positions
        claimPlayer.resetSelection();
        return 1;
    }

    private int createSubzone(ServerCommandSource src, String claimName, AbstractClaim parent, ClaimBox claimBox) throws CommandSyntaxException {
        // Subzone
        ServerPlayerEntity player = src.getPlayerOrThrow();
        for (Subzone subzone : parent.getSubzones()) {
            if (subzone.getBox().intersects(claimBox)) throw INTERSECTS.create(subzone.getFullName());
            if (subzone.getName().equals(claimName)) throw ClaimArgument.NAME_TAKEN;
        }
        validatePermission(src, parent, PermissionManager.MODIFY, Modify.SUBZONE.node());
        Subzone subzone = new Subzone(claimName, ClaimBox.create(claimBox.getMin().withY(parent.getBox().getMinY()), claimBox.getMax().withY(parent.getBox().getMaxY())), player.getServerWorld(), parent);
        ClaimList.addClaim(subzone);
        parent.getMainClaim().show(player, true);
        // reset positions
        ((ClaimPlayer) player).resetSelection();
        return 1;
    }

}
