package me.drex.itsours.command.util;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.drex.itsours.ItsOursMod;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public class ArgumentUtil {

    public static final SuggestionProvider<ServerCommandSource> PLAYERS_PROVIDER = (source, builder) -> {
        List<String> strings = new ArrayList<>();
        for (ServerPlayerEntity player : source.getSource().getMinecraftServer().getPlayerManager().getPlayerList()) {
            strings.add(player.getEntityName());
        }
        return CommandSource.suggestMatching(strings, builder);
    };

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


}
