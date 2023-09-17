package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.placeholders.api.PlaceholderContext;
import me.drex.itsours.data.DataManager;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.GameProfileArgumentType;
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

    public static final BlocksCommand INSTANCE = new BlocksCommand();

    private BlocksCommand() {
        super("blocks");
    }

    private static Map<String, Text> placeholders(int blocks, GameProfile target) {
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
                        argument("blocks", IntegerArgumentType.integer())
                            .executes(ctx -> addBlocks(ctx.getSource(), GameProfileArgumentType.getProfileArgument(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "blocks")))
                    )
                ).requires(src -> Permissions.check(src, "itsours.blocks.add", 2))
            )
            .then(
                literal("check").then(
                    argument("targets", GameProfileArgumentType.gameProfile())
                        .executes(ctx -> checkBlocks(ctx.getSource(), GameProfileArgumentType.getProfileArgument(ctx, "targets")))

                ).requires(src -> Permissions.check(src, "itsours.blocks.check", 2))
            )
            .then(
                literal("set").then(
                    argument("targets", GameProfileArgumentType.gameProfile()).then(
                        argument("blocks", IntegerArgumentType.integer())
                            .executes(ctx -> setBlocks(ctx.getSource(), GameProfileArgumentType.getProfileArgument(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "blocks")))
                    )
                ).requires(src -> Permissions.check(src, "itsours.blocks.set", 2))
            )
            .then(
                literal("give").then(
                    argument("targets", GameProfileArgumentType.gameProfile()).then(
                        argument("blocks", IntegerArgumentType.integer(1))
                            .executes(ctx -> giveBlocks(ctx.getSource(), GameProfileArgumentType.getProfileArgument(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "blocks")))
                    )
                ).requires(src -> Permissions.check(src, "itsours.blocks.give", 2))
            )
            .executes(ctx -> checkBlocks(ctx.getSource(), Collections.singleton(ctx.getSource().getPlayer().getGameProfile())));
    }

    private int checkBlocks(ServerCommandSource src, Collection<GameProfile> targets) {
        int result = 0;
        for (GameProfile target : targets) {
            int blocks = DataManager.getUserData(target.getId()).blocks();
            result += blocks;
            src.sendFeedback(() -> localized("text.itsours.commands.blocks", gameProfile("target_", target)), false);
        }
        return result;
    }

    private int addBlocks(ServerCommandSource src, Collection<GameProfile> targets, int amount) {
        int i = 0;
        for (GameProfile target : targets) {
            int blocks = DataManager.getUserData(target.getId()).blocks();
            int newAmount = MathHelper.clamp(blocks + amount, 0, Integer.MAX_VALUE);
            if (blocks == newAmount) continue;
            DataManager.getUserData(target.getId()).setBlocks(newAmount);
            i++;
            if (amount >= 0) {
                src.sendFeedback(() -> localized("text.itsours.commands.blocks.add", placeholders(amount, target)), false);
            } else {
                src.sendFeedback(() -> localized("text.itsours.commands.blocks.remove", placeholders(-amount, target)), false);
            }
        }
        return i;
    }

    private int giveBlocks(ServerCommandSource src, Collection<GameProfile> targets, int amount) throws CommandSyntaxException {
        int requiredAmount = targets.size() * amount;
        int donatorBlocks = DataManager.getUserData(src.getPlayerOrThrow().getUuid()).blocks();
        if (requiredAmount > donatorBlocks) {
            src.sendError(localized("text.itsours.commands.blocks.give.notEnough"));
            return -1;
        }
        DataManager.getUserData(src.getPlayer().getUuid()).setBlocks(donatorBlocks - requiredAmount);
        int i = 0;
        for (GameProfile target : targets) {
            int receiverBlocks = DataManager.getUserData(target.getId()).blocks();
            int newAmount = Math.min(receiverBlocks + amount, Integer.MAX_VALUE);
            if (receiverBlocks == newAmount) continue;
            DataManager.getUserData(target.getId()).setBlocks(newAmount);
            i++;
            src.sendFeedback(() -> localized("text.itsours.commands.blocks.give", placeholders(amount, target)), false);
            ServerPlayerEntity player = src.getServer().getPlayerManager().getPlayer(target.getId());
            if (player != null)
                player.sendMessage(localized("text.itsours.commands.blocks.give.received", Map.of("blocks", Text.literal(String.valueOf(amount))), PlaceholderContext.of(src)), false);
        }
        return i;
    }

    private int setBlocks(ServerCommandSource src, Collection<GameProfile> targets, int amount) {
        int i = 0;
        for (GameProfile target : targets) {
            int newAmount = MathHelper.clamp(amount, 0, Integer.MAX_VALUE);
            DataManager.getUserData(target.getId()).setBlocks(newAmount);
            src.sendFeedback(() -> localized("text.itsours.commands.blocks.set", placeholders(amount, target)), false);
            i++;
        }
        return i;
    }

}
