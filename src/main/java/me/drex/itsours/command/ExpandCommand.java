package me.drex.itsours.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.util.Modify;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import me.drex.itsours.util.ClaimBox;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;

public class ExpandCommand extends AbstractCommand {

    public static final ExpandCommand EXPAND = new ExpandCommand("expand", true);
    public static final ExpandCommand SHRINK = new ExpandCommand("shrink", false);

    public static final DynamicCommandExceptionType MISSING_CLAIM_BLOCKS = new DynamicCommandExceptionType(blocks -> Text.translatable("text.itsours.commands.expand.missingClaimBlocks", blocks));
    public static final DynamicCommandExceptionType SUBZONE_OUTSIDE = new DynamicCommandExceptionType(name -> Text.translatable("text.itsours.commands.expand.subzoneOutside", name));
    public static final DynamicCommandExceptionType INTERSECTS = new DynamicCommandExceptionType(name -> Text.translatable("text.itsours.commands.expand.intersects", name));
    public static final CommandSyntaxException SHRUNK_TO_FAR = new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.shrink.toFar")).create();
    public static final CommandSyntaxException OUT_OF_WORLD = new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.expand.outOfWorld")).create();

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
        validatePermission(src, claim, PermissionManager.MODIFY, Modify.SIZE.buildNode());
        ServerPlayerEntity player = src.getPlayer();
        ServerWorld world = player.getWorld();
        UUID uuid = player.getUuid();
        Direction direction = Direction.getEntityFacingOrder(player)[0];
        ClaimBox originalBox = claim.getBox();
        int originalArea = originalBox.getArea();
        ClaimBox newBox = originalBox.expand(direction, expand ? distance : -distance);
        int newArea = newBox.getArea();
        if (!expand && !originalBox.contains(newBox)) throw SHRUNK_TO_FAR;
        int areaIncrease = newArea - originalArea;
        for (AbstractClaim other : ClaimList.INSTANCE.getClaims()) {
            if (
                    claim.getDimension().equals(other.getDimension()) &&
                            claim.getDepth() == other.getDepth() &&
                            !claim.equals(other) &&
                            newBox.intersects(other.getBox())
            ) {
                // Idea: draw intersection bounding box
                //ClaimBox intersection = newBox.intersection(other.getBox());
                //intersection.drawOutline(player, Blocks.REDSTONE_BLOCK.getDefaultState());
                other.getBox().drawOutline(player, Blocks.REDSTONE_BLOCK.getDefaultState());
                throw INTERSECTS.create(other.getFullName());
            }
        }
        for (Subzone subzone : claim.getSubzones()) {
            if (!newBox.contains(subzone.getBox())) throw SUBZONE_OUTSIDE.create(subzone.getFullName());
        }
        if (newBox.getMinY() < world.getBottomY() || newBox.getMaxY() > world.getTopY()) throw OUT_OF_WORLD;
        if (claim instanceof Subzone subzone) {
            if (!subzone.getParent().getBox().contains(newBox)) throw SUBZONE_OUTSIDE.create(subzone.getFullName());
        }
        if (claim instanceof Claim) {
            // Check and remove claim blocks
            int blocks = PlayerList.get(uuid, Settings.BLOCKS);
            if (areaIncrease > blocks) throw MISSING_CLAIM_BLOCKS.create(areaIncrease - blocks);
            PlayerList.set(uuid, Settings.BLOCKS, blocks - areaIncrease);
        }
        claim.getMainClaim().show(false);
        claim.setBox(newBox);
        claim.getMainClaim().show(player, true);
        src.sendFeedback(Text.translatable("text.itsours.commands." + literal, claim.getFullName(), distance, direction.getName(), expand ? areaIncrease : -areaIncrease), false);
        return areaIncrease;
    }

}
