package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
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
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            player.executes(ctx -> checkOther(ctx.getSource(), Command.getGameProfile(ctx, "player")));
            LiteralArgumentBuilder<ServerCommandSource> check = LiteralArgumentBuilder.literal("check");
            check.requires(src -> hasPermission(src, "itsours.blocks.check"));
            check.then(player);
            blocks.then(check);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> amount = RequiredArgumentBuilder.argument("amount", IntegerArgumentType.integer());
            amount.executes(ctx -> set(ctx.getSource(), Command.getGameProfile(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount")));
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            LiteralArgumentBuilder<ServerCommandSource> set = LiteralArgumentBuilder.literal("set");
            set.requires(src -> hasPermission(src, "itsours.blocks.set"));
            player.then(amount);
            set.then(player);
            blocks.then(set);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> amount = RequiredArgumentBuilder.argument("amount", IntegerArgumentType.integer());
            amount.executes(ctx -> add(ctx.getSource(), Command.getGameProfile(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount")));
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
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

    public static int checkOther(ServerCommandSource source, GameProfile gameProfile) throws CommandSyntaxException {
        int blocks = PlayerList.get(gameProfile.getId(), Settings.BLOCKS);
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text(gameProfile.getName() + " has " + blocks + " blocks left").color(Color.LIGHT_GREEN));
        return blocks;
    }

    public static int set(ServerCommandSource source, GameProfile gameProfile, int amount) throws CommandSyntaxException {
        PlayerList.set(gameProfile.getId(), Settings.BLOCKS, amount);
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Set " + gameProfile.getName() + "'s claim blocks to " + amount).color(Color.LIGHT_GREEN));
        return amount;
    }

    public static int add(ServerCommandSource source, GameProfile gameProfile, int amount) throws CommandSyntaxException {
        int blocks = PlayerList.get(gameProfile.getId(), Settings.BLOCKS);
        PlayerList.set(gameProfile.getId(), Settings.BLOCKS, Math.max(0, blocks + amount));
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text((amount > 0 ? ("Added " + amount) : ("Removed " + -amount)) + " claim block(s) " + (amount > 0 ? "to " : "from ") + gameProfile.getName()).color(Color.LIGHT_GREEN));
        return amount;
    }

}
