package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.util.Colors;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;

import java.util.*;

public class TrustedCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = ownClaimArgument();
        claim.executes(ctx -> trusted(ctx.getSource(), getClaim(ctx)));
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("trusted");
        command.executes(ctx -> trusted(ctx.getSource(), getAndValidateClaim(ctx.getSource())));
        command.then(claim);
        literal.then(command);
    }

    public static int trusted(ServerCommandSource source, AbstractClaim claim) throws CommandSyntaxException {
        List<UUID> trusted = getAllTrusted(claim);
        if (trusted.isEmpty()) {
            throw new SimpleCommandExceptionType(Text.translatable("text.itsours.command.trusted.no_one", claim.getName())).create();
        } else {
            MutableText text = Text.translatable("text.itsours.command.trusted.title").formatted(Colors.TITLE_COLOR);
            text.append("\n");
            for (int i = 0; i < trusted.size(); i++) {
                text.append(Text.literal(String.valueOf(trusted.get(i))));
                if (i < trusted.size() - 1) text.append(Text.literal(", ").formatted(Formatting.GRAY));
            }
            source.sendFeedback(text, false);
        }
        return trusted.size();
    }

    @Deprecated
    public static Set<UUID> getAllUUIDs(AbstractClaim claim) {
        throw new UnsupportedOperationException();
        /*if (claim instanceof Subzone subzone) {
            Set<UUID> set = new HashSet<>(subzone.getPermissionManager().roleManager.keySet());
            set.addAll(getAllUUIDs(subzone.getParent()));
            return set;
        } else {
            return new HashSet<>(claim.getPermissionManager().roleManager.keySet());
        }*/
    }

    public static List<UUID> getAllTrusted(AbstractClaim claim) {
        List<UUID> trusted = new ArrayList<>();
        for (UUID uuid : getAllUUIDs(claim)) {
            if (claim.getRoles(uuid).containsKey(ItsOurs.INSTANCE.getRoleManager().get("trusted"))) trusted.add(uuid);
        }
        return trusted;
    }


}
