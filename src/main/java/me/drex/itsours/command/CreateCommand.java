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
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import me.drex.itsours.util.ClaimBox;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;

public class CreateCommand extends AbstractCommand {

    public static final CommandSyntaxException SELECT_FIRST = new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.create.selectFirst")).create();
    public static final DynamicCommandExceptionType INTERSECTS = new DynamicCommandExceptionType(name -> Text.translatable("text.itsours.commands.create.intersects", name));
    public static final DynamicCommandExceptionType LIMIT = new DynamicCommandExceptionType(name -> Text.translatable("text.itsours.commands.create.limit", name));
    private static final int MAX_CHECK = 100;

    public static final CreateCommand INSTANCE = new CreateCommand();

    public static final String LITERAL = "create";

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
        // TODO: Make configurable
        int limit = 3;
        if (ItsOurs.hasPermission(src, "max.bypass")) {
            limit = Integer.MAX_VALUE;
        } else {
            for (int i = 0; i < MAX_CHECK; i++) {
                if (ItsOurs.hasPermission(src, "max." + i)) {
                    limit = Math.max(limit, i);
                }
            }
        }

        ServerPlayerEntity player = src.getPlayer();
        UUID uuid = player.getUuid();
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        if (AbstractClaim.isNameInvalid(claimName)) throw ClaimArgument.INVALID_NAME;
        if (!claimPlayer.arePositionsSet()) throw SELECT_FIRST;
        ClaimBox selectedBox = ClaimBox.create(claimPlayer.getFirstPosition().withY(src.getWorld().getBottomY()), claimPlayer.getSecondPosition().withY(src.getWorld().getTopY()));
        Optional<AbstractClaim> optional = ClaimList.INSTANCE.getClaims().stream().filter((claim) ->
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
        if (ClaimList.INSTANCE.getClaimsFrom(src.getPlayer().getUuid()).stream().filter(claim -> claim instanceof Claim).toList().size() >= limit) {
            throw LIMIT.create(limit);
        }
        // Main claim
        Claim claim = new Claim(claimName, uuid, selectedBox, src.getWorld());
        int requiredBlocks = selectedBox.getArea();
        // Check and remove claim blocks
        int blocks = PlayerList.get(uuid, Settings.BLOCKS);
        if (requiredBlocks > blocks) throw ExpandCommand.MISSING_CLAIM_BLOCKS.create(requiredBlocks - blocks);
        if (ClaimList.INSTANCE.getClaim(claimName).isPresent()) throw ClaimArgument.NAME_TAKEN;
        PlayerList.set(uuid, Settings.BLOCKS, blocks - requiredBlocks);
        ClaimList.INSTANCE.addClaim(claim);
        claimPlayer.setLastShow(claim, src.getPlayer().getBlockPos(), src.getWorld());
        claim.show(player, true);
        // reset positions
        claimPlayer.resetSelection();
        return 1;
    }

    private int createSubzone(ServerCommandSource src, String claimName, AbstractClaim parent, ClaimBox claimBox) throws CommandSyntaxException {
        // Subzone
        ServerPlayerEntity player = src.getPlayer();
        for (Subzone subzone : parent.getSubzones()) {
            if (subzone.getBox().intersects(claimBox)) throw INTERSECTS.create(subzone.getFullName());
            if (subzone.getName().equals(claimName)) throw ClaimArgument.NAME_TAKEN;
        }
        validatePermission(src, parent, PermissionManager.MODIFY, Modify.SUBZONE.buildNode());
        Subzone subzone = new Subzone(claimName, player.getUuid(), ClaimBox.create(claimBox.getMin().withY(parent.getBox().getMinY()), claimBox.getMax().withY(parent.getBox().getMaxY())), player.getWorld(), parent);
        ClaimList.INSTANCE.addClaim(subzone);
        parent.getMainClaim().show(player, true);
        // reset positions
        ((ClaimPlayer) player).resetSelection();
        return 1;
    }

}
