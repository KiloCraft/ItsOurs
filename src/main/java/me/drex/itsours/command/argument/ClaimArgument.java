package me.drex.itsours.command.argument;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.Subzone;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;

import java.util.*;

import static me.drex.itsours.util.LocalizedMessage.localized;
import static net.minecraft.commands.Commands.argument;

public class ClaimArgument {

    public static final CommandSyntaxException INVALID_NAME = new SimpleCommandExceptionType(localized("text.itsours.argument.claim.invalidName")).create();
    public static final CommandSyntaxException NAME_TAKEN = new SimpleCommandExceptionType(localized("text.itsours.argument.claim.duplicate")).create();
    public static final DynamicCommandExceptionType UNKNOWN_CLAIM = new DynamicCommandExceptionType(name -> localized("text.itsours.argument.claim.notFound", Map.of("input", Component.literal(name.toString()))));
    public static final SuggestionProvider<CommandSourceStack> ALL_CLAIMS_PROVIDER = (source, builder) -> {
        final List<String> result = new ArrayList<>();
        for (AbstractClaim claim : ClaimList.getClaims().stream().filter(claim -> claim instanceof Claim).toList()) {
            result.add(claim.getFullName());
            addSubzones(claim, builder.getRemaining(), result);
        }
        return SharedSuggestionProvider.suggest(result, builder);
    };
    public static final SuggestionProvider<CommandSourceStack> OWN_CLAIMS_PROVIDER = (source, builder) -> {
        final List<String> result = new ArrayList<>();
        ServerPlayer player = source.getSource().getPlayer();
        ClaimList.getClaimAt(player)
            .ifPresent(claim -> result.add(claim.getFullName()));
        UUID uuid = player.getUUID();
        if (uuid != null) {
            for (Claim claim : ClaimList.getClaimsFrom(uuid)) {
                result.add(claim.getFullName());
                addSubzones(claim, builder.getRemaining(), result);
            }
        }
        return SharedSuggestionProvider.suggest(result, builder);
    };
    private static final String DEFAULT_NAME = "claim";

    public static RequiredArgumentBuilder<CommandSourceStack, String> ownClaims() {
        return ownClaims(DEFAULT_NAME);
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> allClaims() {
        return allClaims(DEFAULT_NAME);
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> allClaims(String name) {
        return argument(name, StringArgumentType.word()).suggests(ALL_CLAIMS_PROVIDER);
    }

    public static RequiredArgumentBuilder<CommandSourceStack, String> ownClaims(String name) {
        return argument(name, StringArgumentType.word()).suggests(OWN_CLAIMS_PROVIDER);
    }

    public static AbstractClaim getClaim(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return getClaim(ctx, DEFAULT_NAME);
    }

    public static AbstractClaim getClaim(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        String string = StringArgumentType.getString(ctx, name);
        Optional<? extends AbstractClaim> optional = ClaimList.getClaim(string);
        return optional.orElseThrow(() -> UNKNOWN_CLAIM.create(string));
    }

    private static void addSubzones(AbstractClaim claim, String input, List<String> result) {
        if (input.startsWith(claim.getFullName())) {
            for (Subzone subzone : claim.getSubzones()) {
                result.add(subzone.getFullName());
                addSubzones(subzone, input, result);
            }
        }
    }
}
