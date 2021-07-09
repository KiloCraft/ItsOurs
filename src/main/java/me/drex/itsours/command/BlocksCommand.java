package me.drex.itsours.command;

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
import net.minecraft.server.command.ServerCommandSource;

public class BlocksCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralArgumentBuilder<ServerCommandSource> blocks = LiteralArgumentBuilder.literal("blocks");
        blocks.executes(context -> check(context.getSource()));
        {
            RequiredArgumentBuilder<ServerCommandSource, String> player = playerArgument("player");
            player.executes(BlocksCommand::checkOther);
            LiteralArgumentBuilder<ServerCommandSource> check = LiteralArgumentBuilder.literal("check");
            check.requires(src -> hasPermission(src, "itsours.blocks.check"));
            check.then(player);
            blocks.then(check);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> amount = RequiredArgumentBuilder.argument("amount", IntegerArgumentType.integer());
            amount.executes(BlocksCommand::set);
            RequiredArgumentBuilder<ServerCommandSource, String> player = playerArgument("player");
            LiteralArgumentBuilder<ServerCommandSource> set = LiteralArgumentBuilder.literal("set");
            set.requires(src -> hasPermission(src, "itsours.blocks.set"));
            player.then(amount);
            set.then(player);
            blocks.then(set);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> amount = RequiredArgumentBuilder.argument("amount", IntegerArgumentType.integer());
            amount.executes(BlocksCommand::add);
            RequiredArgumentBuilder<ServerCommandSource, String> player = playerArgument("player");
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

    public static int checkOther(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        getGameProfile(ctx, "player", profile -> {
            int blocks = PlayerList.get(profile.getId(), Settings.BLOCKS);
            ((ClaimPlayer) ctx.getSource().getPlayer()).sendMessage(Component.text(profile.getName() + " has " + blocks + " blocks left").color(Color.LIGHT_GREEN));
        });
        return 1;
    }

    public static int set(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        getGameProfile(ctx, "player", profile -> {
            PlayerList.set(profile.getId(), Settings.BLOCKS, amount);
            ((ClaimPlayer) ctx.getSource().getPlayer()).sendMessage(Component.text("Set " + profile.getName() + "'s claim blocks to " + amount).color(Color.LIGHT_GREEN));
        });
        return 1;
    }

    public static int add(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        getGameProfile(ctx, "player", profile -> {
            int blocks = PlayerList.get(profile.getId(), Settings.BLOCKS);
            PlayerList.set(profile.getId(), Settings.BLOCKS, Math.max(0, blocks + amount));
            ((ClaimPlayer) ctx.getSource().getPlayer()).sendMessage(Component.text((amount > 0 ? ("Added " + amount) : ("Removed " + -amount)) + " claim block(s) " + (amount > 0 ? "to " : "from ") + profile.getName()).color(Color.LIGHT_GREEN));
        });
        return 1;
    }

}
