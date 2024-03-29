package me.drex.itsours.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import me.drex.itsours.util.Color;
import me.drex.itsours.util.TextComponentUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.server.command.ServerCommandSource;
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
        ItsOursMod mod = ItsOursMod.INSTANCE;
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
        if (mod.getClaimList().get(uuid).stream().filter(claim -> claim instanceof Claim).count() >= limit) {
            throw new SimpleCommandExceptionType(TextComponentUtil.error("You can't have more than " + limit + " claims")).create();
        }
        ClaimPlayer claimPlayer = (ClaimPlayer) source.getPlayer();
        if (claimPlayer.arePositionsSet()) {
            BlockPos min = new BlockPos(claimPlayer.getLeftPosition());
            min = new BlockPos(min.getX(), source.getWorld().getBottomY(), min.getZ());
            BlockPos max = new BlockPos(claimPlayer.getRightPosition());
            max = new BlockPos(max.getX(), source.getWorld().getTopY(), max.getZ());
            if (AbstractClaim.isNameInvalid(name))
                throw new SimpleCommandExceptionType(TextComponentUtil.error("Claim name is to long or contains invalid characters")).create();
            AbstractClaim claim = new Claim(name, uuid, min, max, source.getWorld(), null);
            AbstractClaim show = claim;
            if (claim.intersects().isPresent()) {
                Optional<AbstractClaim> parent = mod.getClaimList().get(source.getWorld(), min);
                if (parent.isPresent() && parent.get().contains(max)) {
                    if (parent.get().getDepth() > 2)
                        throw new SimpleCommandExceptionType(TextComponentUtil.error("You can't create subzones with a depth higher than 3")).create();
                    validatePermission(parent.get(), uuid, "modify.subzone");
                    for (Subzone subzone : parent.get().getSubzones()) {
                        if (subzone.getName().equals(name))
                            throw new SimpleCommandExceptionType(TextComponentUtil.error("Claim name is already taken")).create();
                    }
                    claim = new Subzone(name, uuid, min, max, source.getWorld(), null, parent.get());
                    show = parent.get();
                } else {
                    throw new SimpleCommandExceptionType(TextComponentUtil.error("Claim couldn't be created, because it would overlap with another claim")).create();
                }
            } else {
                if (PlayerList.get(uuid, Settings.BLOCKS) < claim.getArea())
                    throw new SimpleCommandExceptionType(TextComponentUtil.error("You need " + (claim.getArea() - PlayerList.get(uuid, Settings.BLOCKS)) + " more claim blocks")).create();
                if (mod.getClaimList().contains(name))
                    throw new SimpleCommandExceptionType(TextComponentUtil.error("Claim name is already taken")).create();
                PlayerList.set(uuid, Settings.BLOCKS, PlayerList.get(uuid, Settings.BLOCKS) - claim.getArea());
                BlockPos size = claim.getSize();
                ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Claim " + name + " has been created (" + size.getX() + " x " + size.getY() + " x " + size.getZ() + ")").color(Color.LIGHT_GREEN));
            }
            if (claimPlayer.getLastShowClaim() != null) claimPlayer.getLastShowClaim().show(source.getPlayer(), false);
            claimPlayer.setLastShow(show, source.getPlayer().getBlockPos(), source.getWorld());
            mod.getClaimList().add(claim);
            show.show(source.getPlayer(), true);


            //reset positions
            claimPlayer.setLeftPosition(null);
            claimPlayer.setRightPosition(null);
            claimPlayer.setSelecting(false);
            return 1;
        } else {
            claimPlayer.sendMessage(Component.text("You need to select the corners of your claim with a golden shovel (left- / rightclick) first.").color(Color.RED));
            return 0;
        }

    }
}
