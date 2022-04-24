package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BlocksCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> blocks = LiteralArgumentBuilder.literal("blocks");
        blocks.executes(context -> check(context.getSource()));
        {
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = players();
            player.executes(BlocksCommand::checkOther);
            LiteralArgumentBuilder<ServerCommandSource> check = LiteralArgumentBuilder.literal("check");
            check.requires(src -> hasPermission(src, "itsours.blocks.check"));
            check.then(player);
            blocks.then(check);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> amount = RequiredArgumentBuilder.argument("amount", IntegerArgumentType.integer());
            amount.executes(BlocksCommand::set);
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = players();
            LiteralArgumentBuilder<ServerCommandSource> set = LiteralArgumentBuilder.literal("set");
            set.requires(src -> hasPermission(src, "itsours.blocks.set"));
            player.then(amount);
            set.then(player);
            blocks.then(set);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> amount = RequiredArgumentBuilder.argument("amount", IntegerArgumentType.integer());
            amount.executes(BlocksCommand::add);
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = players();
            LiteralArgumentBuilder<ServerCommandSource> add = LiteralArgumentBuilder.literal("add");
            add.requires(src -> hasPermission(src, "itsours.blocks.add"));
            player.then(amount);
            add.then(player);
            blocks.then(add);
        }
        command.then(blocks);
    }

    public static int check(ServerCommandSource source) throws CommandSyntaxException {
        int blocks = PlayerList.get(source.getPlayer().getUuid(), Settings.BLOCKS);
        source.sendFeedback(Text.translatable("text.itsours.command.blocks.check.self", blocks).formatted(Formatting.GREEN), false);
        return blocks;
    }

    public static int checkOther(CommandContext<ServerCommandSource> ctx) {
        getGameProfiles(ctx).thenAccept(gameProfiles -> {
            for (GameProfile profile : gameProfiles) {
                int blocks = PlayerList.get(profile.getId(), Settings.BLOCKS);
                ctx.getSource().sendFeedback(Text.translatable("text.itsours.command.blocks.check.other", profile.getName(), blocks).formatted(Formatting.GREEN), false);
            }
        });
        return 1;
    }

    public static int set(CommandContext<ServerCommandSource> ctx) {
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        getGameProfiles(ctx).thenAccept(gameProfiles -> {
            for (GameProfile profile : gameProfiles) {
                PlayerList.set(profile.getId(), Settings.BLOCKS, amount);
                ctx.getSource().sendFeedback(Text.translatable("text.itsours.command.blocks.set", profile.getName(), amount).formatted(Formatting.GREEN), false);
            }
        });
        return 1;
    }

    public static int add(CommandContext<ServerCommandSource> ctx) {
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        getGameProfiles(ctx).thenAccept(gameProfiles -> {
            for (GameProfile profile : gameProfiles) {
                int blocks = PlayerList.get(profile.getId(), Settings.BLOCKS);
                PlayerList.set(profile.getId(), Settings.BLOCKS, Math.max(0, blocks + amount));
                MutableText text;
                if (amount >= 0) {
                    text = Text.translatable("text.itsours.command.blocks.add", amount, profile.getName()).formatted(Formatting.GREEN);
                } else {
                    text = Text.translatable("text.itsours.command.blocks.remove", -amount, profile.getName()).formatted(Formatting.GREEN);
                }
                ctx.getSource().sendFeedback(text, false);
            }
        });
        return 1;
    }

}
