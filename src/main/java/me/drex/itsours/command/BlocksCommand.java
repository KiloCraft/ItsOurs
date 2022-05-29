package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Settings;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;
import java.util.Collections;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BlocksCommand extends AbstractCommand {

    public static final BlocksCommand INSTANCE = new BlocksCommand();

    private BlocksCommand() {
        super("blocks");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(
                        literal("add").then(
                                argument("targets", GameProfileArgumentType.gameProfile()).then(
                                        argument("blocks", IntegerArgumentType.integer())
                                                .executes(ctx -> addBlocks(ctx.getSource(), GameProfileArgumentType.getProfileArgument(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "blocks")))
                                )
                        ).requires(src -> Permissions.check(src, "itsours.blocks.add"))
                )
                .then(literal("check").then(
                                argument("targets", GameProfileArgumentType.gameProfile())
                                        .executes(ctx -> checkBlocks(ctx.getSource(), GameProfileArgumentType.getProfileArgument(ctx, "targets")))

                        )
                ).requires(src -> Permissions.check(src, "itsours.blocks.check"))
                .then(
                        // TODO:
                        literal("set").then(
                                argument("targets", GameProfileArgumentType.gameProfile()).then(
                                        argument("blocks", IntegerArgumentType.integer())
                                                .executes(ctx -> setBlocks(ctx.getSource(), GameProfileArgumentType.getProfileArgument(ctx, "targets"), IntegerArgumentType.getInteger(ctx, "blocks")))
                                )
                        ).requires(src -> Permissions.check(src, "itsours.blocks.set"))
                )
                .executes(ctx -> checkBlocks(ctx.getSource(), Collections.singleton(ctx.getSource().getPlayer().getGameProfile())));
    }

    private int checkBlocks(ServerCommandSource src, Collection<GameProfile> targets) {
        int result = 0;
        for (GameProfile target : targets) {
            int blocks = PlayerList.get(target.getId(), Settings.BLOCKS);
            result += blocks;
            src.sendFeedback(Text.translatable("text.itsours.commands.blocks", Texts.toText(target), blocks), false);
        }
        return result;
    }

    private int addBlocks(ServerCommandSource src, Collection<GameProfile> targets, int amount) {
        int i = 0;
        for (GameProfile target : targets) {
            int blocks = PlayerList.get(target.getId(), Settings.BLOCKS);
            int newAmount = MathHelper.clamp(blocks + amount, 0, Integer.MAX_VALUE);
            if (blocks == newAmount) continue;
            PlayerList.set(target.getId(), Settings.BLOCKS, newAmount);
            i++;
            if (amount >= 0) {
                src.sendFeedback(Text.translatable("text.itsours.commands.blocks.add", amount, Texts.toText(target)), false);
            } else {
                src.sendFeedback(Text.translatable("text.itsours.commands.blocks.remove", -amount, Texts.toText(target)), false);
            }
        }
        return i;
    }

    private int setBlocks(ServerCommandSource src, Collection<GameProfile> targets, int amount) {
        int i = 0;
        for (GameProfile target : targets) {
            int newAmount = MathHelper.clamp(amount, 0, Integer.MAX_VALUE);
            PlayerList.set(target.getId(), Settings.BLOCKS, newAmount);
            src.sendFeedback(Text.translatable("text.itsours.commands.blocks.set", Texts.toText(target), amount), false);
            i++;
        }
        return i;
    }

}
