package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.command.util.ArgumentUtil;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class BlocksCommand extends Command {

    public void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        //TODO: Check if player has permission to execute the commands
        LiteralArgumentBuilder<ServerCommandSource> blocks = LiteralArgumentBuilder.literal("blocks");
        blocks.executes(context -> check(context.getSource()));
        {
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            player.executes(ctx -> checkOther(ctx.getSource(), ArgumentUtil.getGameProfile(ctx, "player")));
            LiteralArgumentBuilder<ServerCommandSource> check = LiteralArgumentBuilder.literal("check");
            check.requires(src -> this.hasPermission(src, "itsours.blocks.check"));
            check.then(player);
            blocks.then(check);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> amount = RequiredArgumentBuilder.argument("amount", IntegerArgumentType.integer());
            amount.executes(ctx -> set(ctx.getSource(), ArgumentUtil.getGameProfile(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount")));
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            LiteralArgumentBuilder<ServerCommandSource> set = LiteralArgumentBuilder.literal("set");
            set.requires(src -> this.hasPermission(src, "itsours.blocks.set"));
            player.then(amount);
            set.then(player);
            blocks.then(set);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> amount = RequiredArgumentBuilder.argument("amount", IntegerArgumentType.integer());
            amount.executes(ctx -> add(ctx.getSource(), ArgumentUtil.getGameProfile(ctx, "player"), IntegerArgumentType.getInteger(ctx, "amount")));
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            LiteralArgumentBuilder<ServerCommandSource> add = LiteralArgumentBuilder.literal("add");
            add.requires(src -> this.hasPermission(src, "itsours.blocks.add"));
            player.then(amount);
            add.then(player);
            blocks.then(add);
        }
        command.then(blocks);
    }

    public int check(ServerCommandSource source) throws CommandSyntaxException {
        int blocks = ItsOursMod.INSTANCE.getBlockManager().getBlocks(source.getPlayer().getUuid());
        ((ClaimPlayer) source.getPlayer()).sendMessage(new LiteralText("You have " + blocks + " blocks left").formatted(Formatting.GREEN));
        return blocks;
    }

    public int checkOther(ServerCommandSource source, GameProfile gameProfile) throws CommandSyntaxException {
        int blocks = ItsOursMod.INSTANCE.getBlockManager().getBlocks(gameProfile.getId());
        ((ClaimPlayer) source.getPlayer()).sendMessage(new LiteralText(gameProfile.getName() + " has " + blocks + " blocks left").formatted(Formatting.GREEN));
        return blocks;
    }

    public int set(ServerCommandSource source, GameProfile gameProfile, int amount) throws CommandSyntaxException {
        ItsOursMod.INSTANCE.getBlockManager().setBlocks(gameProfile.getId(), amount);
        ((ClaimPlayer) source.getPlayer()).sendMessage(new LiteralText("Set " + gameProfile.getName() + "'s claim blocks to " + amount).formatted(Formatting.GREEN));
        return amount;
    }

    public int add(ServerCommandSource source, GameProfile gameProfile, int amount) throws CommandSyntaxException {
        ItsOursMod.INSTANCE.getBlockManager().addBlocks(gameProfile.getId(), amount);
        ((ClaimPlayer) source.getPlayer()).sendMessage(new LiteralText((amount > 0 ? ("Added " + amount) : ("Removed " + -amount)) + " claim block(s) " + (amount > 0 ? "to " : "from ") + gameProfile.getName()).formatted(Formatting.GREEN));
        return amount;
    }

}
