package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.WorldUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class InfoCommand extends Command {

    public void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = claimArgument();
        claim.executes(ctx -> info(ctx.getSource(), getClaim(ctx)));
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("info");
        command.executes(ctx -> info(ctx.getSource(), this.getAndValidateClaim(ctx.getSource().getWorld(), ctx.getSource().getPlayer().getBlockPos())));
        command.then(claim);
        literal.then(command);
    }

    public int info(ServerCommandSource source, AbstractClaim claim) throws CommandSyntaxException {
        UUID ownerUUID = claim.getOwner();
        String ownerName = "";
        GameProfile owner = source.getMinecraftServer().getUserCache().getByUuid(ownerUUID);
        if (owner != null && owner.isComplete()) {
            ownerName = owner.getName();
        }
        BlockPos size = claim.getSize();

        MutableText text = new LiteralText("\n");
        text.append(new LiteralText("Claim Info: ").formatted(Formatting.GOLD))
                .append(new LiteralText("\n"))
                .append(newInfoLine("Name", new LiteralText(claim.getName()).formatted(Formatting.WHITE)))
                .append(newInfoLine("Owner", ownerName.equals("") ?
                        new LiteralText(ownerUUID.toString()).formatted(Formatting.RED, Formatting.ITALIC).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, claim.getOwner().toString()))) :
                        new LiteralText(ownerName).formatted(Formatting.GOLD)))
                .append(newInfoLine("Size", new LiteralText(size.getX() + " x " + size.getY() + " x " + size.getZ()).formatted(Formatting.GREEN)))
                .append(new LiteralText("").append(new LiteralText("* Flags:").formatted(Formatting.YELLOW))
                        .append(new LiteralText(" ...")).append(new LiteralText("\n")));
        //TODO: List flags

        MutableText pos = new LiteralText("");
        Text min = newPosLine(claim.min, Formatting.AQUA, Formatting.DARK_AQUA);
        Text max = newPosLine(claim.max, Formatting.LIGHT_PURPLE, Formatting.DARK_PURPLE);


        pos.append(newInfoLine("Position", new LiteralText("")
                .append(new LiteralText("Min ").formatted(Formatting.WHITE).append(min))
                .append(new LiteralText(" "))
                .append(new LiteralText("Max ").formatted(Formatting.WHITE).append(max))));
        text.append(pos);
        text.append(newInfoLine("Dimension", new LiteralText(WorldUtil.toIdentifier(claim.getWorld()))));
        ((ClaimPlayer) source.getPlayer()).sendMessage(text);

        return 1;
    }

    private MutableText newPosLine(BlockPos pos, Formatting form1, Formatting form2) {
        return new LiteralText("")
                .append(new LiteralText(String.valueOf(pos.getX())).formatted(form1))
                .append(new LiteralText(" "))
                .append(new LiteralText(String.valueOf(pos.getY())).formatted(form2))
                .append(new LiteralText(" "))
                .append(new LiteralText(String.valueOf(pos.getZ())).formatted(form1));
    }

    private MutableText newInfoLine(String title, Text... text) {
        MutableText message = new LiteralText("").append(new LiteralText("* " + title + ": ").formatted(Formatting.YELLOW));
        for (Text t : text) {
            message.append(t);
        }
        return message.append(new LiteralText("\n"));
    }
}