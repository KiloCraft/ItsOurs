package me.drex.itsours.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.claim.flags.FlagsManager;
import me.drex.itsours.claim.flags.util.Modify;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.util.ClaimBox;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

import static me.drex.itsours.util.PlaceholderUtil.mergePlaceholderMaps;
import static me.drex.message.api.LocalizedMessage.localized;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.text.Text.literal;

public class ExpandCommand extends AbstractCommand {

    public static final ExpandCommand EXPAND = new ExpandCommand("expand", true);
    public static final ExpandCommand SHRINK = new ExpandCommand("shrink", false);

    private final boolean expand;

    private ExpandCommand(@NotNull String literal, boolean expand) {
        super(literal);
        this.expand = expand;
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(
            argument("distance", IntegerArgumentType.integer(1))
                .executes(ctx -> execute(ctx.getSource(), getClaim(ctx.getSource().getEntity()), IntegerArgumentType.getInteger(ctx, "distance")))
        );
    }

    private int execute(ServerCommandSource src, AbstractClaim claim, int distance) throws CommandSyntaxException {
        validateAction(src, claim, FlagsManager.MODIFY, Modify.SIZE.node());
        ServerPlayerEntity player = src.getPlayerOrThrow();
        ServerWorld world = player.getServerWorld();
        UUID uuid = player.getUuid();
        Direction direction = Direction.getEntityFacingOrder(player)[0];
        ClaimBox originalBox = claim.getBox();
        int originalArea = originalBox.getArea();
        ClaimBox newBox = originalBox.expand(direction, expand ? distance : -distance);
        int newArea = newBox.getArea();
        if (!expand && !originalBox.contains(newBox)) {
            src.sendError(localized("text.itsours.commands.shrink.shrunkToFar"));
            return 0;
        }
        int areaIncrease = newArea - originalArea;
        for (AbstractClaim other : ClaimList.getClaims()) {
            if (
                claim.getDimension().equals(other.getDimension()) &&
                    claim.getDepth() == other.getDepth() &&
                    !claim.equals(other) &&
                    newBox.intersects(other.getBox())
            ) {
                src.sendError(localized("text.itsours.commands.expand.intersects", other.placeholders(src.getServer())));
                return 0;
            }
        }
        for (Subzone subzone : claim.getSubzones()) {
            if (!newBox.contains(subzone.getBox())) {
                src.sendError(localized("text.itsours.commands.expand.subzoneOutside", mergePlaceholderMaps(
                    subzone.placeholders(src.getServer(), "subzone_"),
                    claim.placeholders(src.getServer())
                )));
                return 0;
            }
        }
        if (newBox.getMinY() < world.getBottomY() || newBox.getMaxY() > world.getTopY()) {
            src.sendError(localized("text.itsours.commands.expand.outOfWorld"));
            return 0;
        }
        if (claim instanceof Subzone subzone) {
            if (!subzone.getParent().getBox().contains(newBox)) {
                src.sendError(localized("text.itsours.commands.expand.subzoneOutside", mergePlaceholderMaps(
                    subzone.placeholders(src.getServer(), "subzone_"),
                    subzone.getParent().placeholders(src.getServer())
                )));
                return 0;
            }
        }
        if (claim instanceof Claim) {
            // Check and remove claim blocks
            int blocks = DataManager.getUserData(uuid).blocks();
            if (areaIncrease > blocks) {
                src.sendError(localized("text.itsours.commands.expand.missingClaimBlocks", Map.of("blocks", literal(String.valueOf(areaIncrease - blocks)))));
                return 0;
            }
            DataManager.updateUserData(uuid).setBlocks(blocks - areaIncrease);
        }

        claim.setBox(newBox);
        claim.getMainClaim().notifyTrackingChanges(src.getServer());

        src.sendFeedback(() -> localized("text.itsours.commands." + literal, mergePlaceholderMaps(
                Map.of(
                    "distance", literal(String.valueOf(distance)),
                    "direction", literal(direction.getName()),
                    "blocks", literal(String.valueOf(expand ? areaIncrease : -areaIncrease))
                ),
                claim.placeholders(src.getServer())
            )
        ), false);
        return areaIncrease;
    }

}
