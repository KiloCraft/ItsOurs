package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;

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
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("You have " + blocks + " blocks left").color(Color.LIGHT_GREEN));
        return blocks;
    }

    public static int checkOther(CommandContext<ServerCommandSource> ctx) {
        getGameProfiles(ctx).thenAccept(gameProfiles -> {
            for (GameProfile profile : gameProfiles) {
                int blocks = PlayerList.get(profile.getId(), Settings.BLOCKS);
                sendFeedback(ctx.getSource(), Component.text(profile.getName() + " has " + blocks + " blocks left").color(Color.LIGHT_GREEN));
            }
        });
        return 1;
    }

    public static int set(CommandContext<ServerCommandSource> ctx) {
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        getGameProfiles(ctx).thenAccept(gameProfiles -> {
            for (GameProfile profile : gameProfiles) {
                PlayerList.set(profile.getId(), Settings.BLOCKS, amount);
                sendFeedback(ctx.getSource(), Component.text("Set " + profile.getName() + "'s claim blocks to " + amount).color(Color.LIGHT_GREEN));
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
                sendFeedback(ctx.getSource(), Component.text((amount > 0 ? ("Added " + amount) : ("Removed " + -amount)) + " claim block(s) " + (amount > 0 ? "to " : "from ") + profile.getName()).color(Color.LIGHT_GREEN));
            }
        });
        return 1;
    }

}
