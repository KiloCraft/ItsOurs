package me.drex.itsours.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;
import java.util.UUID;

public class CreateCommand extends Command {

    private static final int MAX_CHECK = 100;

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> name = RequiredArgumentBuilder.argument("name", StringArgumentType.word());
        name.executes(ctx -> create(ctx.getSource(), StringArgumentType.getString(ctx, "name")));
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("create");
        command.executes(ctx -> create(ctx.getSource(), ctx.getSource().getPlayer().getEntityName()));
        command.then(name);
        literal.then(command);
    }

    public static int create(ServerCommandSource source, String name) throws CommandSyntaxException {
        UUID uuid = source.getPlayer().getUuid();
        //TODO: Make configurable
        int limit = 3;
        if (hasPermission(source, "itsours.max.bypass")) {
            limit = Integer.MAX_VALUE;
        } else {
            for (int i = 0; i < MAX_CHECK; i++) {
                if (hasPermission(source, "itsours.max." + i)) {
                    limit = Math.max(limit, i);
                }
            }
        }
        if (ClaimList.INSTANCE.getClaimsFrom(uuid).stream().filter(claim -> claim instanceof Claim).count() >= limit) {
            throw new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.exception.claim_limit")).create();
        }
        ClaimPlayer claimPlayer = (ClaimPlayer) source.getPlayer();
        if (claimPlayer.arePositionsSet()) {
            BlockPos min = new BlockPos(claimPlayer.getFirstPosition());
            min = new BlockPos(min.getX(), source.getWorld().getBottomY(), min.getZ());
            BlockPos max = new BlockPos(claimPlayer.getSecondPosition());
            max = new BlockPos(max.getX(), source.getWorld().getTopY(), max.getZ());
            if (AbstractClaim.isNameInvalid(name))
                throw new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.exception.invalid_name")).create();
            AbstractClaim claim = new Claim(name, uuid, min, max, source.getWorld(), null);
            AbstractClaim show = claim;
            if (claim.intersects().isPresent()) {
                Optional<AbstractClaim> parent = ClaimList.INSTANCE.getClaimAt(source.getWorld(), min);
                if (parent.isPresent() && parent.get().contains(max)) {
                    if (parent.get().getDepth() > 2)
                        throw new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.exception.invalid_depth")).create();
                    validatePermission(parent.get(), source, "modify.subzone");
                    for (Subzone subzone : parent.get().getSubzones()) {
                        if (subzone.getName().equals(name))
                            throw new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.exception.name_taken")).create();
                    }
                    claim = new Subzone(name, uuid, min, max, source.getWorld(), null, parent.get());
                    show = parent.get();
                } else {
                    throw new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.exception.create_intersects")).create();
                }
            } else {
                if (PlayerList.get(uuid, Settings.BLOCKS) < claim.getArea())
                    throw new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.exception.create.missing_blocks", (claim.getArea() - PlayerList.get(uuid, Settings.BLOCKS)))).create();
                if (ClaimList.INSTANCE.getClaim(name).isPresent())
                    throw new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.exception.name_taken")).create();
                PlayerList.set(uuid, Settings.BLOCKS, PlayerList.get(uuid, Settings.BLOCKS) - claim.getArea());
                BlockPos size = claim.getSize();
                source.sendFeedback(Text.translatable("text.itsours.command.create", name, size.getZ(), size.getY(), size.getZ()).formatted(Formatting.GREEN), false);
            }
            if (claimPlayer.getLastShowClaim() != null) claimPlayer.getLastShowClaim().show(source.getPlayer(), false);
            claimPlayer.setLastShow(show, source.getPlayer().getBlockPos(), source.getWorld());
            ClaimList.INSTANCE.addClaim(claim);
            show.show(source.getPlayer(), true);


            //reset positions
            claimPlayer.setSecondPosition(null);
            claimPlayer.setFirstPosition(null);
            claimPlayer.setSelecting(false);
            return 1;
        } else {
            source.sendFeedback(Text.translatable("text.itsours.command.create.select_notice"), false);
            return 0;
        }

    }
}
