package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.data.DataManager;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static me.drex.itsours.util.PlaceholderUtil.gameProfile;
import static me.drex.itsours.util.PlaceholderUtil.mergePlaceholderMaps;
import static me.drex.message.api.LocalizedMessage.localized;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BlocksCommand extends AbstractCommand {

    public static final CommandSyntaxException OVERFLOW = new SimpleCommandExceptionType(localized("text.itsours.argument.general.overflow")).create();

    public static final BlocksCommand INSTANCE = new BlocksCommand();

    private BlocksCommand() {
        super("blocks");
    }

    private static Map<String, Text> placeholders(long blocks, PlayerConfigEntry target) {
        return mergePlaceholderMaps(
            Map.of("blocks", Text.literal(String.valueOf(blocks))),
            gameProfile("target_", target)
        );
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(
                literal("add").then(
                    argument("targets", GameProfileArgumentType.gameProfile()).then(
                        argument("blocks", LongArgumentType.longArg(1))
                            .executes(ctx -> addBlocks(ctx.getSource(), GameProfileArgumentType.getProfileArgument(ctx, "targets"), LongArgumentType.getLong(ctx, "blocks")))
                    )
                ).requires(src -> ItsOurs.checkPermission(src, "itsours.blocks.add", 2))
            ).then(
                literal("remove").then(
                    argument("targets", GameProfileArgumentType.gameProfile()).then(
                        argument("blocks", LongArgumentType.longArg(1))
                            .executes(ctx -> addBlocks(ctx.getSource(), GameProfileArgumentType.getProfileArgument(ctx, "targets"), -LongArgumentType.getLong(ctx, "blocks")))
                    )
                ).requires(src -> ItsOurs.checkPermission(src, "itsours.blocks.remove", 2))
            )
            .then(
                literal("check").then(
                    argument("targets", GameProfileArgumentType.gameProfile())
                        .executes(ctx -> checkBlocks(ctx.getSource(), GameProfileArgumentType.getProfileArgument(ctx, "targets")))

                ).requires(src -> ItsOurs.checkPermission(src, "itsours.blocks.check", 2))
            )
            .then(
                literal("set").then(
                    argument("targets", GameProfileArgumentType.gameProfile()).then(
                        argument("blocks", LongArgumentType.longArg(0))
                            .executes(ctx -> setBlocks(ctx.getSource(), GameProfileArgumentType.getProfileArgument(ctx, "targets"), LongArgumentType.getLong(ctx, "blocks")))
                    )
                ).requires(src -> ItsOurs.checkPermission(src, "itsours.blocks.set", 2))
            )
            .then(
                literal("give").then(
                    argument("targets", GameProfileArgumentType.gameProfile()).then(
                        argument("blocks", LongArgumentType.longArg(1))
                            .executes(ctx -> giveBlocks(ctx.getSource(), GameProfileArgumentType.getProfileArgument(ctx, "targets"), LongArgumentType.getLong(ctx, "blocks")))
                    )
                ).requires(src -> ItsOurs.checkPermission(src, "itsours.blocks.give", 2))
            )
            .executes(ctx -> checkBlocks(ctx.getSource(), Collections.singleton(ctx.getSource().getPlayerOrThrow().getPlayerConfigEntry())));
    }

    private int checkBlocks(ServerCommandSource src, Collection<PlayerConfigEntry> targets) {
        long result = 0;
        for (PlayerConfigEntry target : targets) {
            long blocks = DataManager.getUserData(target.id()).blocks();
            result += blocks;
            src.sendFeedback(() -> localized("text.itsours.commands.blocks", placeholders(blocks, target)), false);
        }
        return (int) result;
    }

    private int addBlocks(ServerCommandSource src, Collection<PlayerConfigEntry> targets, long amount) throws CommandSyntaxException {

        try {
            long[] newAmounts = new long[targets.size()];
            int i = 0;
            for (PlayerConfigEntry target : targets) {
                long blocks = DataManager.getUserData(target.id()).blocks();
                newAmounts[i] = Math.max(0, Math.addExact(blocks, amount));
                i++;
            }

            i = 0;
            for (PlayerConfigEntry target : targets) {
                DataManager.updateUserData(target.id()).setBlocks(newAmounts[i]);
                if (amount >= 0) {
                    src.sendFeedback(() -> localized("text.itsours.commands.blocks.add", placeholders(amount, target)), false);
                } else {
                    src.sendFeedback(() -> localized("text.itsours.commands.blocks.remove", placeholders(-amount, target)), false);
                }
                i++;
            }
            return i;

        } catch (ArithmeticException e) {
            throw OVERFLOW;
        }
    }

    private int giveBlocks(ServerCommandSource src, Collection<PlayerConfigEntry> targets, long amount) throws CommandSyntaxException {
        try {
            long requiredAmount = Math.multiplyExact(amount, targets.size());
            long donatorBlocks = DataManager.getUserData(src.getPlayerOrThrow().getUuid()).blocks();
            if (requiredAmount > donatorBlocks) {
                src.sendError(localized("text.itsours.commands.blocks.give.notEnough"));
                return -1;
            }
            long[] newAmounts = new long[targets.size()];
            int i = 0;
            for (PlayerConfigEntry target : targets) {
                long receiverBlocks = DataManager.getUserData(target.id()).blocks();
                newAmounts[i] = Math.addExact(receiverBlocks, amount);
                i++;
            }

            DataManager.updateUserData(src.getPlayer().getUuid()).setBlocks(donatorBlocks - requiredAmount);

            i = 0;
            for (PlayerConfigEntry target : targets) {
                DataManager.updateUserData(target.id()).setBlocks(newAmounts[i]);
                src.sendFeedback(() -> localized("text.itsours.commands.blocks.give", placeholders(amount, target)), false);
                ServerPlayerEntity player = src.getServer().getPlayerManager().getPlayer(target.id());
                if (player != null)
                    player.sendMessage(localized("text.itsours.commands.blocks.give.received", Map.of("blocks", Text.literal(String.valueOf(amount))), PlaceholderContext.of(src)), false);
                i++;
            }
            return i;
        } catch (ArithmeticException e) {
            throw OVERFLOW;
        }
    }

    private int setBlocks(ServerCommandSource src, Collection<PlayerConfigEntry> targets, long amount) {
        int i = 0;
        for (PlayerConfigEntry target : targets) {
            long newAmount = Math.max(amount, 0);
            DataManager.updateUserData(target.id()).setBlocks(newAmount);
            src.sendFeedback(() -> localized("text.itsours.commands.blocks.set", placeholders(amount, target)), false);
            i++;
        }
        return i;
    }

}
