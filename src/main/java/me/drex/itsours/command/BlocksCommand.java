package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.data.DataManager;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.players.NameAndId;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static me.drex.itsours.util.PlaceholderUtil.gameProfile;
import static me.drex.itsours.util.PlaceholderUtil.mergePlaceholderMaps;
import static me.drex.itsours.util.LocalizedMessage.localized;
import static me.drex.itsours.util.LocalizedMessage.sendMessage;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class BlocksCommand extends AbstractCommand {

    public static final CommandSyntaxException OVERFLOW = new SimpleCommandExceptionType(localized("text.itsours.argument.general.overflow")).create();

    public static final BlocksCommand INSTANCE = new BlocksCommand();

    private BlocksCommand() {
        super("blocks");
    }

    private static Map<String, Component> placeholders(long blocks, NameAndId target) {
        return mergePlaceholderMaps(
            Map.of("blocks", Component.literal(String.valueOf(blocks))),
            gameProfile("target_", target)
        );
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        literal.then(
                literal("add").then(
                    argument("targets", GameProfileArgument.gameProfile()).then(
                        argument("blocks", LongArgumentType.longArg(1))
                            .executes(ctx -> addBlocks(ctx.getSource(), GameProfileArgument.getGameProfiles(ctx, "targets"), LongArgumentType.getLong(ctx, "blocks")))
                    )
                ).requires(src -> ItsOurs.checkPermission(src, "itsours.blocks.add", 2))
            ).then(
                literal("remove").then(
                    argument("targets", GameProfileArgument.gameProfile()).then(
                        argument("blocks", LongArgumentType.longArg(1))
                            .executes(ctx -> addBlocks(ctx.getSource(), GameProfileArgument.getGameProfiles(ctx, "targets"), -LongArgumentType.getLong(ctx, "blocks")))
                    )
                ).requires(src -> ItsOurs.checkPermission(src, "itsours.blocks.remove", 2))
            )
            .then(
                literal("check").then(
                    argument("targets", GameProfileArgument.gameProfile())
                        .executes(ctx -> checkBlocks(ctx.getSource(), GameProfileArgument.getGameProfiles(ctx, "targets")))

                ).requires(src -> ItsOurs.checkPermission(src, "itsours.blocks.check", 2))
            )
            .then(
                literal("set").then(
                    argument("targets", GameProfileArgument.gameProfile()).then(
                        argument("blocks", LongArgumentType.longArg(0))
                            .executes(ctx -> setBlocks(ctx.getSource(), GameProfileArgument.getGameProfiles(ctx, "targets"), LongArgumentType.getLong(ctx, "blocks")))
                    )
                ).requires(src -> ItsOurs.checkPermission(src, "itsours.blocks.set", 2))
            )
            .then(
                literal("give").then(
                    argument("targets", GameProfileArgument.gameProfile()).then(
                        argument("blocks", LongArgumentType.longArg(1))
                            .executes(ctx -> giveBlocks(ctx.getSource(), GameProfileArgument.getGameProfiles(ctx, "targets"), LongArgumentType.getLong(ctx, "blocks")))
                    )
                ).requires(src -> ItsOurs.checkPermission(src, "itsours.blocks.give", 2))
            )
            .executes(ctx -> checkBlocks(ctx.getSource(), Collections.singleton(ctx.getSource().getPlayerOrException().nameAndId())));
    }

    private int checkBlocks(CommandSourceStack src, Collection<NameAndId> targets) {
        long result = 0;
        for (NameAndId target : targets) {
            long blocks = DataManager.getUserData(target.id()).blocks();
            result += blocks;
            src.sendSuccess(() -> localized("text.itsours.commands.blocks", placeholders(blocks, target)), false);
        }
        return (int) result;
    }

    private int addBlocks(CommandSourceStack src, Collection<NameAndId> targets, long amount) throws CommandSyntaxException {

        try {
            long[] newAmounts = new long[targets.size()];
            int i = 0;
            for (NameAndId target : targets) {
                long blocks = DataManager.getUserData(target.id()).blocks();
                newAmounts[i] = Math.max(0, Math.addExact(blocks, amount));
                i++;
            }

            i = 0;
            for (NameAndId target : targets) {
                DataManager.updateUserData(target.id()).setBlocks(newAmounts[i]);
                if (amount >= 0) {
                    src.sendSuccess(() -> localized("text.itsours.commands.blocks.add", placeholders(amount, target)), false);
                } else {
                    src.sendSuccess(() -> localized("text.itsours.commands.blocks.remove", placeholders(-amount, target)), false);
                }
                i++;
            }
            return i;

        } catch (ArithmeticException e) {
            throw OVERFLOW;
        }
    }

    private int giveBlocks(CommandSourceStack src, Collection<NameAndId> targets, long amount) throws CommandSyntaxException {
        try {
            long requiredAmount = Math.multiplyExact(amount, targets.size());
            long donatorBlocks = DataManager.getUserData(src.getPlayerOrException().getUUID()).blocks();
            if (requiredAmount > donatorBlocks) {
                src.sendFailure(localized("text.itsours.commands.blocks.give.notEnough"));
                return -1;
            }
            long[] newAmounts = new long[targets.size()];
            int i = 0;
            for (NameAndId target : targets) {
                long receiverBlocks = DataManager.getUserData(target.id()).blocks();
                newAmounts[i] = Math.addExact(receiverBlocks, amount);
                i++;
            }

            DataManager.updateUserData(src.getPlayer().getUUID()).setBlocks(donatorBlocks - requiredAmount);

            i = 0;
            for (NameAndId target : targets) {
                DataManager.updateUserData(target.id()).setBlocks(newAmounts[i]);
                src.sendSuccess(() -> localized("text.itsours.commands.blocks.give", placeholders(amount, target)), false);
                ServerPlayer player = src.getServer().getPlayerList().getPlayer(target.id());
                if (player != null)
                    sendMessage(player, localized("text.itsours.commands.blocks.give.received", Map.of("blocks", Component.literal(String.valueOf(amount))), player), false);
                i++;
            }
            return i;
        } catch (ArithmeticException e) {
            throw OVERFLOW;
        }
    }

    private int setBlocks(CommandSourceStack src, Collection<NameAndId> targets, long amount) {
        int i = 0;
        for (NameAndId target : targets) {
            long newAmount = Math.max(amount, 0);
            DataManager.updateUserData(target.id()).setBlocks(newAmount);
            src.sendSuccess(() -> localized("text.itsours.commands.blocks.set", placeholders(amount, target)), false);
            i++;
        }
        return i;
    }

}
