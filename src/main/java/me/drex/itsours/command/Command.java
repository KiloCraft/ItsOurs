package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;

public abstract class Command {

    //TODO: Look at this again, maybe there is a better approach to this
    public static GameProfile getGameProfile(CommandContext<ServerCommandSource> ctx, String name) throws CommandSyntaxException {
        AtomicReference<String> exception = new AtomicReference<>();
        CompletableFuture<GameProfile> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Collection<GameProfile> profiles = GameProfileArgumentType.getProfileArgument(ctx, name);
                if (profiles.size() > 1) {
                    exception.set("Only one selection is allowed!");
                } else if (profiles.isEmpty()) {
                    exception.set("At least one selection is required!");
                }
                return profiles.iterator().next();
            } catch (CommandSyntaxException e) {
                exception.set(e.getRawMessage().getString());
            }
            return null;
        });
        GameProfile profile = null;
        try {
            profile = completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            ItsOursMod.LOGGER.error("Unable to retrieve GameProfile: ", e);
        }
        if (exception.get() != null) throw new SimpleCommandExceptionType(new LiteralText(exception.get())).create();
        return profile;
    }

    AbstractClaim getAndValidateClaim(ServerWorld world, BlockPos pos) throws CommandSyntaxException {
        AbstractClaim claim = ItsOursMod.INSTANCE.getClaimList().get(world, pos);
        if (claim == null) throw new SimpleCommandExceptionType(new LiteralText("Couldn't find a claim at your position!")).create();
        return claim;
    }

    boolean hasPermission(ServerCommandSource src, String permission) {
        return ItsOursMod.INSTANCE.getPermissionHandler().hasPermission(src, permission, 2);
    }

    public AbstractClaim getClaim(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "claim");
        for (AbstractClaim claim : ItsOursMod.INSTANCE.getClaimList().get()) {
            if (claim.getName().equals(name)) return claim;
        }
        throw new SimpleCommandExceptionType(new LiteralText("Couldn't find a claim with that name")).create();
    }

    public RequiredArgumentBuilder<ServerCommandSource, String> claimArgument() {
        return argument("claim", word()).suggests(CLAIM_PROVIDER);
    }

    public final SuggestionProvider<ServerCommandSource> CLAIM_PROVIDER = (source, builder) -> {
        UUID uuid = source.getSource().getPlayer().getUuid();
        ServerPlayerEntity player = source.getSource().getPlayer();
        List<String> names = new ArrayList<>();
        AbstractClaim current = ItsOursMod.INSTANCE.getClaimList().get(player.getServerWorld(), player.getBlockPos());
        if (current != null) names.add(current.getName());
        if (uuid != null) {
            for (AbstractClaim claim : ItsOursMod.INSTANCE.getClaimList().get(uuid)) {
                names.add(claim.getName());
            }
        }
        return CommandSource.suggestMatching(names, builder);
    };




}
