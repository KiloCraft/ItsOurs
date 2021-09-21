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
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionList;
import me.drex.itsours.claim.permission.util.node.util.Node;
import me.drex.itsours.command.help.HelpCategory;
import me.drex.itsours.command.util.SafeConsumer;
import me.drex.itsours.util.PermissionHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
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
        Optional<AbstractClaim> current = ItsOursMod.INSTANCE.getClaimList().get(player.getServerWorld(), player.getBlockPos());
        current.ifPresent(claim -> names.add(claim.getFullName()));
        if (uuid != null) {
            for (AbstractClaim claim : ItsOursMod.INSTANCE.getClaimList().get(uuid).stream().filter(claim -> claim instanceof Claim).collect(Collectors.toList())) {
                names.add(claim.getFullName());
                addSubzones(claim, builder.getRemaining(), names);
            }
        }
        return CommandSource.suggestMatching(names, builder);
    };

    public static final SuggestionProvider<ServerCommandSource> ALL_CLAIM_PROVIDER = (source, builder) -> {
        List<String> names = new ArrayList<>();
        for (AbstractClaim claim : ItsOursMod.INSTANCE.getClaimList().get().stream().filter(claim -> claim instanceof Claim).collect(Collectors.toList())) {
            names.add(claim.getFullName());
            addSubzones(claim, builder.getRemaining(), names);
        }
        return CommandSource.suggestMatching(names, builder);
    };

    public static final SuggestionProvider<ServerCommandSource> PERMISSION_PROVIDER = (source, builder) -> {
        List<String> permissions = new ArrayList<>(getPermissions(PermissionList.permission, "", builder.getRemaining()));
        return CommandSource.suggestMatching(permissions, builder);
    };
    public static final SuggestionProvider<ServerCommandSource> SETTING_PROVIDER = (source, builder) -> {
        List<String> settings = new ArrayList<>(getPermissions(PermissionList.setting, "", builder.getRemaining()));
        settings.addAll(getPermissions(PermissionList.permission, "", builder.getRemaining()));
        return CommandSource.suggestMatching(settings, builder);
    };

    public static final SuggestionProvider<ServerCommandSource> PLAYER_PROVIDER = (source, builder) -> {
        List<String> players = new ArrayList<>();
        for (ServerPlayerEntity player : source.getSource().getServer().getPlayerManager().getPlayerList()) {
            players.add(player.getEntityName());
        }
        return CommandSource.suggestMatching(players, builder);
    };

    public static final SuggestionProvider<ServerCommandSource> PERMISSION_VALUE_PROVIDER = (source, builder) -> CommandSource.suggestMatching(Arrays.asList("true", "false", "unset"), builder);

    public static final SuggestionProvider<ServerCommandSource> CATEGORY_PROVIDER = (context, builder) -> {
        List<String> categories = new ArrayList<>();
        for (HelpCategory value : HelpCategory.values()) {
            categories.add(value.getId());
        }
        return CommandSource.suggestMatching(categories, builder);
    };

    public static void getGameProfile(CommandContext<ServerCommandSource> ctx, String argument, SafeConsumer<GameProfile> consumer) {
        CompletableFuture.runAsync(() -> {
            String error;
            try {
                String playerName = StringArgumentType.getString(ctx, argument);
                MinecraftServer server = ctx.getSource().getServer();
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);
                if (player != null) {
                    consumer.accept(player.getGameProfile());
                    return;
                }
                Optional<GameProfile> optional = server.getUserCache().findByName(playerName);
                if (optional.isPresent()) {
                    consumer.accept(optional.get());
                    return;
                } else {
                    error = "Unknown player!";
                }
            } catch (Exception e) {
                error = e.getMessage();
            }
            if (error != null) {
                ctx.getSource().sendError(new LiteralText(error));
            }
        });
    }

    private static List<String> getPermissions(Node node, String currentID, String input) {
        ArrayList<String> list = new ArrayList<>();
        String newID = currentID.equals("") ? node.getId() : currentID + "." + node.getId();
        list.add(newID);
        if (newID.equals("") || !newID.startsWith(input)) {
            for (Node n : node.getNodes()) {
                list.addAll(getPermissions(n, newID, input));
            }
        }
        return list;
    }

    private static void addSubzones(AbstractClaim claim, String input, List<String> names) {
        addSubzones(claim, input, names, c -> true);
    }

    private static void addSubzones(AbstractClaim claim, String input, List<String> names, Predicate<AbstractClaim> predicate) {
        if (input.startsWith(claim.getFullName())) {
            for (Subzone subzone : claim.getSubzones()) {
                if (predicate.test(subzone)) names.add(subzone.getFullName());
                addSubzones(subzone, input, names);
            }
        }
    }

    public static AbstractClaim getAndValidateClaim(ServerWorld world, BlockPos pos) throws CommandSyntaxException {
        Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get(world, pos);
        if (!claim.isPresent())
            throw new SimpleCommandExceptionType(new LiteralText("Couldn't find a claim at your position!")).create();
        return claim.get();
    }

    protected static AbstractClaim getAndValidateClaim(ServerCommandSource src) throws CommandSyntaxException {
        return getAndValidateClaim(src.getWorld(), src.getPlayer().getBlockPos());
    }

    protected static boolean hasPermission(ServerCommandSource src, String permission) {
        return PermissionHandler.hasPermission(src, permission, 2);
    }

    static void validatePermission(AbstractClaim claim, UUID uuid, String permission) throws CommandSyntaxException {
        if (!claim.hasPermission(uuid, permission))
            throw new SimpleCommandExceptionType(new LiteralText("You don't have permission to do that")).create();
    }

    public static AbstractClaim getClaim(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "claim");
        Optional<? extends AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get(name);
        if (claim.isPresent()) return claim.get();
        throw new SimpleCommandExceptionType(new LiteralText("Couldn't find a claim with that name")).create();
    }

    public static Permission getPermission(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Optional<Permission> permission = Permission.permission(StringArgumentType.getString(ctx, "perm"));
        if (!permission.isPresent())
            throw new SimpleCommandExceptionType(new LiteralText("Invalid permission")).create();
        return permission.get();
    }

    public static Permission getSetting(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Optional<Permission> setting = Permission.setting(StringArgumentType.getString(ctx, "setting"));
        if (!setting.isPresent())
            throw new SimpleCommandExceptionType(new LiteralText("Invalid setting")).create();
        return setting.get();
    }

    public static Permission.Value getPermissionValue(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String value = StringArgumentType.getString(ctx, "value");
        for (Permission.Value val : Permission.Value.values()) {
            if (val.name.equalsIgnoreCase(value)) return val;
        }
        throw new SimpleCommandExceptionType(new LiteralText("Invalid permission value")).create();
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> permissionClaimArgument(String... permissions) {

        SuggestionProvider<ServerCommandSource> CLAIM_PROVIDER = (source, builder) -> {
            UUID uuid = source.getSource().getPlayer().getUuid();
            Predicate<AbstractClaim> predicate = claim -> {
                for (String permission : permissions) {
                    if (claim.hasPermission(uuid, permission)) return true;
                }
                return false;
            };
            List<String> names = new ArrayList<>();
            if (uuid != null) {
                for (AbstractClaim claim : ItsOursMod.INSTANCE.getClaimList().get().stream().filter(claim -> claim instanceof Claim).collect(Collectors.toList())) {
                    if (predicate.test(claim)) names.add(claim.getFullName());
                    addSubzones(claim, builder.getRemaining(), names, predicate);
                }
            }
            return CommandSource.suggestMatching(names, builder);
        };
        return argument("claim", word()).suggests(CLAIM_PROVIDER);
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> ownClaimArgument() {
        return argument("claim", word()).suggests(OWN_CLAIM_PROVIDER);
    }

    public static RequiredArgumentBuilder<ServerCommandSource, String> categoryArgument() {
        return argument("category", word()).suggests(CATEGORY_PROVIDER);
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

    public static RequiredArgumentBuilder<ServerCommandSource, String> playerArgument(String name) {
        return argument(name, word()).suggests(PLAYER_PROVIDER);
    }

}
