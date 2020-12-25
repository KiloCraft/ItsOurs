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
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class RemoveCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        LiteralArgumentBuilder<ServerCommandSource> confirm = LiteralArgumentBuilder.literal("confirm");
        confirm.executes(ctx -> remove(ctx.getSource(), getClaim(ctx)));
        RequiredArgumentBuilder<ServerCommandSource, String> claim = ownClaimArgument();
        claim.executes(ctx -> requestRemove(ctx.getSource(), getClaim(ctx)));
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("remove");
        claim.then(confirm);
        command.then(claim);
        literal.then(command);
    }

    public static int requestRemove(ServerCommandSource source, AbstractClaim claim) throws CommandSyntaxException {
       validate(source, claim);
       if (!source.getPlayer().getUuid().equals(claim.getOwner())) {
           ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("WARNING: This is not your claim...").color(Color.RED).decorate(TextDecoration.BOLD));
       }
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Are you sure you want to delete the claim \"" + claim.getName() + "\"? ").color(Color.RED)
                .append(Component.text("[I'M SURE]").clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/claim remove " + claim.getName() + " confirm")).decorate(TextDecoration.BOLD).color(Color.RED)));
       return 0;
    }

    public static int remove(ServerCommandSource source, AbstractClaim claim) throws CommandSyntaxException {
        validate(source, claim);
        //Remove claim from it's parents subzone list, so the garbage collector can remove the claim
        if (claim instanceof Subzone) {
            ((Subzone) claim).getParent().removeSubzone((Subzone) claim);
        }
        if (claim instanceof Claim) {
            int blocks = ItsOursMod.INSTANCE.getPlayerList().getBlocks(claim.getOwner());
            ItsOursMod.INSTANCE.getPlayerList().setBlocks(claim.getOwner(), Math.max(0, blocks + claim.getArea()));
        }
        claim.show(false);
        //recursively remove all subzones
        removeSubzones(claim);
        ItsOursMod.INSTANCE.getClaimList().remove(claim);
        ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Deleted the claim \"" + claim.getName() + "\" ").color(Color.LIGHT_GREEN));

        return 0;
    }

    public static void removeSubzones(AbstractClaim claim) {
        for (Subzone subzone : claim.getSubzones()) {
            if (!subzone.getSubzones().isEmpty()) removeSubzones(subzone);
            ItsOursMod.INSTANCE.getClaimList().remove(subzone);
        }
    }

    //TODO
    public static void validate(ServerCommandSource source, AbstractClaim claim) throws CommandSyntaxException {
        if (!source.getPlayer().getUuid().equals(claim.getOwner()) && !hasPermission(source, "itsours.remove")) {
            throw new SimpleCommandExceptionType(new LiteralText("You can't delete that claim")).create();
        }
    }

}
