package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class RemoveCommand extends Command {

    public void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        LiteralArgumentBuilder<ServerCommandSource> confirm = LiteralArgumentBuilder.literal("confirm");
        confirm.executes(ctx -> remove(ctx.getSource(), getClaim(ctx)));
        RequiredArgumentBuilder<ServerCommandSource, String> claim = claimArgument();
        claim.executes(ctx -> requestRemove(ctx.getSource(), getClaim(ctx)));
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("remove");
        claim.then(confirm);
        command.then(claim);
        literal.then(command);
    }

    public int requestRemove(ServerCommandSource source, AbstractClaim claim) throws CommandSyntaxException {
       validate(source, claim);
       if (!source.getPlayer().getUuid().toString().equals(claim.getOwner().toString())) {
           ((ClaimPlayer) source.getPlayer()).sendMessage(new LiteralText("WARNING: This is not your claim...").formatted(Formatting.DARK_RED).formatted(Formatting.BOLD));
       }
        ((ClaimPlayer) source.getPlayer()).sendMessage(new LiteralText("Are you sure you want to delete the claim \"" + claim.getName() + "\"? ").formatted(Formatting.RED)
                .append(new LiteralText("[I'M SURE]").styled(style -> style
                        .withColor(Formatting.DARK_RED)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/claim remove " + claim.getName() + " confirm")))));
       return 0;
    }

    public int remove(ServerCommandSource source, AbstractClaim claim) throws CommandSyntaxException {
        validate(source, claim);
        //Remove claim from it's parents subzone list, so the garbage collector can remove the claim
        if (claim instanceof Subzone) {
            ((Subzone) claim).getParent().removeSubzone((Subzone) claim);
        }
        if (claim instanceof Claim) {
            ItsOursMod.INSTANCE.getBlockManager().addBlocks(claim.getOwner(), claim.getArea());
        }
        claim.show(null);
        //recursively remove all subzones
        removeSubzones(claim);
        ItsOursMod.INSTANCE.getClaimList().remove(claim);
        ((ClaimPlayer) source.getPlayer()).sendMessage(new LiteralText("Deleted the claim \"" + claim.getName() + "\" ").formatted(Formatting.GREEN));

        return 0;
    }

    public void removeSubzones(AbstractClaim claim) {
        for (Subzone subzone : claim.getSubzones()) {
            if (!subzone.getSubzones().isEmpty()) removeSubzones(subzone);
            ItsOursMod.INSTANCE.getClaimList().remove(subzone);
        }
    }

    public void validate(ServerCommandSource source, AbstractClaim claim) throws CommandSyntaxException {
        if (source.getPlayer().getUuid() != claim.getOwner() && !hasPermission(source, "itsours.remove")) {
            throw new SimpleCommandExceptionType(new LiteralText("You can't delete that claim")).create();
        }
    }

}
