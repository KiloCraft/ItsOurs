package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class BlocksCommand extends Command {

    public void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("blocks");
        command.executes(context -> check(context.getSource()));
        literal.then(command);
    }

    public int check(ServerCommandSource source) throws CommandSyntaxException {
        int blocks = ItsOursMod.INSTANCE.getBlockManager().getBlocks(source.getPlayer().getUuid());
        ((ClaimPlayer) source.getPlayer()).sendMessage(new LiteralText("You have " + blocks + " blocks left").formatted(Formatting.GREEN));
        return blocks;
    }

}
