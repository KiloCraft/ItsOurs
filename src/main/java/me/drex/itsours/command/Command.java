package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.claim.permission.util.Group;
import me.drex.itsours.claim.permission.util.Permission;
import me.drex.itsours.claim.permission.util.Setting;
import me.drex.itsours.claim.permission.util.node.AbstractNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;

public abstract class Command {

    public static final SuggestionProvider<ServerCommandSource> ROLE_PROVIDER = (source, builder) -> {
        List<String> names = new ArrayList<>();
        ItsOursMod.INSTANCE.getRoleManager().forEach((roleID, role) -> names.add(roleID));
        return CommandSource.suggestMatching(names, builder);
    };
    public static final SuggestionProvider<ServerCommandSource> OWN_CLAIM_PROVIDER = (source, builder) -> {
        UUID uuid = source.getSource().getPlayer().getUuid();
        ServerPlayerEntity player = source.getSource().getPlayer();
        List<String> names = new ArrayList<>();
        AbstractClaim current = ItsOursMod.INSTANCE.getClaimList().get(player.getServerWorld(), player.getBlockPos());
        if (current != null) names.add(current.getName());
        if (uuid != null) {
            for (AbstractClaim claim : ItsOursMod.INSTANCE.getClaimList().get(uuid).stream().filter(claim -> claim instanceof Claim).collect(Collectors.toList())) {
                names.add(claim.getName());
                addSubzones(claim, builder.getRemaining(), names);
            }
        }
        return CommandSource.suggestMatching(names, builder);
    };

    public static final SuggestionProvider<ServerCommandSource> ALL_CLAIM_PROVIDER = (source, builder) -> {
        List<String> names = new ArrayList<>();
        for (AbstractClaim claim : ItsOursMod.INSTANCE.getClaimList().get().stream().filter(claim -> claim instanceof Claim).collect(Collectors.toList())) {
            names.add(claim.getName());
            addSubzones(claim, builder.getRemaining(), names);
        }
        return CommandSource.suggestMatching(names, builder);
    };

    public static final SuggestionProvider<ServerCommandSource> PERMISSION_PROVIDER = (source, builder) -> {
        List<String> permissions = new ArrayList<>();
        for (Permission permission : Permission.permissions) {
            if (permission instanceof Setting) continue;
            permissions.add(permission.id);
            if (builder.getRemaining().startsWith(permission.id) && permission.groups.length > 0) {
                addNodes(permission.id, permission.groups, 0, builder.getRemaining(), permissions);
            }
        }
        return CommandSource.suggestMatching(permissions, builder);
    };

    public static final SuggestionProvider<ServerCommandSource> SETTING_PROVIDER = (source, builder) -> {
        List<String> settings = new ArrayList<>();
        for (Permission setting : Permission.permissions) {
            settings.add(setting.id);
            if (builder.getRemaining().startsWith(setting.id) && setting.groups.length > 0) {
                addNodes(setting.id, setting.groups, 0, builder.getRemaining(), settings);
            }
        }
        return CommandSource.suggestMatching(settings, builder);
    };

    public static final SuggestionProvider<ServerCommandSource> PERMISSION_VALUE_PROVIDER = (source, builder) -> CommandSource.suggestMatching(Arrays.asList("true", "false", "unset"), builder);

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

    private static void addSubzones(AbstractClaim claim, String input, List<String> names) {
        if (input.startsWith(claim.getFullName())) {
            for (Subzone subzone : claim.getSubzones()) {
                names.add(subzone.getFullName());
                addSubzones(subzone, input, names);
            }
        }
    }

    private static void addNodes(String parent, Group[] groups, int i, String input, List<String> permissions) {
        for (AbstractNode node : groups[i].list) {
            String s = parent + "." + node.getID();
            permissions.add(s);
            if (input.startsWith(s) && i + 1 < groups.length) addNodes(s, groups, i + 1, input, permissions);
        }
    }

    static AbstractClaim getAndValidateClaim(ServerWorld world, BlockPos pos) throws CommandSyntaxException {
        AbstractClaim claim = ItsOursMod.INSTANCE.getClaimList().get(world, pos);
        if (claim == null)
            throw new SimpleCommandExceptionType(new LiteralText("Couldn't find a claim at your position!")).create();
        return claim;
    }

    static boolean hasPermission(ServerCommandSource src, String permission) {
        return ItsOursMod.INSTANCE.getPermissionHandler().hasPermission(src, permission, 2);
    }

    static void validatePermission(AbstractClaim claim, UUID uuid, String permission) throws CommandSyntaxException {
        if (!claim.hasPermission(uuid, permission))
        throw new SimpleCommandExceptionType(new LiteralText("You don't have permission to do that")).create();
    }

    public static AbstractClaim getClaim(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "claim");
        AbstractClaim claim = ItsOursMod.INSTANCE.getClaimList().get(name);
        if (claim != null) return claim;
        throw new SimpleCommandExceptionType(new LiteralText("Couldn't find a claim with that name")).create();
    }

    public static String getPermission(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String permission = StringArgumentType.getString(ctx, "perm");
        if (!Permission.isValid(permission)) throw new SimpleCommandExceptionType(new LiteralText("Invalid permission")).create();
        return permission;
    }

    public static String getSetting(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String permission = StringArgumentType.getString(ctx, "setting");
        if (!Setting.isValid(permission)) throw new SimpleCommandExceptionType(new LiteralText("Invalid setting")).create();
        return permission;
    }

    public static Permission.Value getPermissionValue(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String value = StringArgumentType.getString(ctx, "value");
        for (Permission.Value val : Permission.Value.values()) {
            if (val.name.equalsIgnoreCase(value)) return val;
        }
        throw new SimpleCommandExceptionType(new LiteralText("Invalid permission value")).create();
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> ownClaimArgument() {
        return argument("claim", word()).suggests(OWN_CLAIM_PROVIDER);
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> allClaimArgument() {
        return argument("claim", word()).suggests(ALL_CLAIM_PROVIDER);
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> roleArgument() {
        return argument("name", word()).suggests(ROLE_PROVIDER);
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> permissionArgument() {
        return argument("perm", word()).suggests(PERMISSION_PROVIDER);
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> settingArgument() {
        return argument("setting", word()).suggests(SETTING_PROVIDER);
    }


    public static RequiredArgumentBuilder<ServerCommandSource, String> permissionValueArgument() {
        return argument("value", word()).suggests(PERMISSION_VALUE_PROVIDER);
    }


}
