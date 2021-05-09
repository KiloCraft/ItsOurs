package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import me.drex.itsours.util.TextComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TrustedCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = ownClaimArgument();
        claim.executes(ctx -> trusted(ctx.getSource(), getClaim(ctx)));
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("trusted");
        command.executes(ctx -> trusted(ctx.getSource(), getAndValidateClaim(ctx.getSource().getWorld(), ctx.getSource().getPlayer().getBlockPos())));
        command.then(claim);
        literal.then(command);
    }

    public static int trusted(ServerCommandSource source, AbstractClaim claim) throws CommandSyntaxException {
        List<UUID> trusted = new ArrayList<>();
        for (UUID uuid : getAllUUIDs(claim)) {
            if (claim.getRoles(uuid).containsKey(ItsOursMod.INSTANCE.getRoleManager().get("trusted"))) trusted.add(uuid);
        }
        ClaimPlayer player = (ClaimPlayer) source.getPlayer();
        if (trusted.isEmpty()) {
            player.sendMessage(Component.text("No one is trusted in " + claim.getName() + ".").color(Color.RED));
        } else {
            TextComponent.Builder builder = Component.text().content("Trusted:\n").color(Color.ORANGE);
            for (int i = 0; i < trusted.size(); i++) {
                builder.append(TextComponentUtil.toName(trusted.get(i), Color.YELLOW));
                if (i + 1 < trusted.size()) builder.append(Component.text(", ").color(Color.GRAY));
            }
            player.sendMessage(builder.build());
        }
        return trusted.size();
    }

    private static Set<UUID> getAllUUIDs(AbstractClaim claim) {
        if (claim instanceof Subzone) {
            Subzone subzone = (Subzone) claim;
            Set<UUID> set = subzone.getPermissionManager().roleManager.keySet();
            set.addAll(getAllUUIDs(subzone.getParent()));
            return set;
        } else {
            return claim.getPermissionManager().roleManager.keySet();
        }
    }


}
