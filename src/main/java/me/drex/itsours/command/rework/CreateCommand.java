package me.drex.itsours.command.rework;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.command.rework.argument.ClaimArgument;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import me.drex.itsours.util.ClaimBox;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;

public class CreateCommand extends AbstractCommand {

    public static final CommandSyntaxException SELECT_FIRST = new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.create.selectFirst")).create();
    public static final DynamicCommandExceptionType INTERSECTS = new DynamicCommandExceptionType(name -> Text.translatable("text.itsours.commands.create.intersects", name));

    public static final CreateCommand INSTANCE = new CreateCommand();

    private CreateCommand() {
        super("create");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(
                argument("name", StringArgumentType.word())
                        .executes(ctx -> executeCreate(ctx.getSource(), StringArgumentType.getString(ctx, "name")))
        ).executes(ctx -> executeCreate(ctx.getSource(), ctx.getSource().getName()));
    }

    private int executeCreate(ServerCommandSource src, String claimName) throws CommandSyntaxException {
        // TODO: Check max claim limit
        ServerPlayerEntity player = src.getPlayer();
        UUID uuid = player.getUuid();
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        if (AbstractClaim.isNameInvalid(claimName)) throw ClaimArgument.INVALID_NAME;
        if (!claimPlayer.arePositionsSet()) throw SELECT_FIRST;
        ClaimBox selectedBox = ClaimBox.create(claimPlayer.getFirstPosition().withY(src.getWorld().getBottomY()), claimPlayer.getSecondPosition().withY(src.getWorld().getTopY()));
        for (AbstractClaim claim : ClaimList.INSTANCE.getClaims()) {
            if (
                    claim.getDimension().equals(player.getWorld().getRegistryKey()) &&
                            claim.getDepth() == 0 &&
                            selectedBox.intersects(claim.getBox())
            ) {
                if (claim.getBox().contains(selectedBox)) {
                    return createSubzone(player, claimName, claim, selectedBox);
                } else {
                    throw INTERSECTS.create(claim.getFullName());
                }
            }
        }
        // Main claim
        Claim claim = new Claim(claimName, uuid, selectedBox.getMin(), selectedBox.getMax(), src.getWorld());
        int requiredBlocks = selectedBox.getArea();
        // Check and remove claim blocks
        int blocks = PlayerList.get(uuid, Settings.BLOCKS);
        if (requiredBlocks > blocks) throw ExpandCommand.MISSING_CLAIM_BLOCKS.create(requiredBlocks - blocks);
        PlayerList.set(uuid, Settings.BLOCKS, blocks - requiredBlocks);
        // TODO: Name taken
        if (ClaimList.INSTANCE.getClaim(claimName).isPresent()) throw ClaimArgument.NAME_TAKEN;
        ClaimList.INSTANCE.addClaim(claim);
        claim.show(player, true);
        return 1;
    }

    private int createSubzone(ServerPlayerEntity player, String claimName, AbstractClaim parent, ClaimBox claimBox) throws CommandSyntaxException {
        // Subzone
        // TODO: Change constructor to take box
        for (Subzone subzone : parent.getSubzones()) {
            if (subzone.getBox().intersects(claimBox)) throw INTERSECTS.create(subzone.getFullName());
            if (subzone.getName().equals(claimName)) throw ClaimArgument.NAME_TAKEN;
        }
        // TODO: Validate permission
        Subzone subzone = new Subzone(claimName, player.getUuid(), claimBox.getMin().withY(parent.getBox().getMinY()), claimBox.getMax().withY(parent.getBox().getMaxY()), player.getWorld(), parent);
        ClaimList.INSTANCE.addClaim(subzone);
        parent.getMainClaim().show(player, true);
        return 1;
    }

}
