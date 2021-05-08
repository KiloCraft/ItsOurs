package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;


public class RoleCommand extends Command {

    public static final SuggestionProvider<ServerCommandSource> REMOVED_ROLES_PROVIDER = (source, builder) -> {
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Role> entry : ItsOursMod.INSTANCE.getRoleManager().entrySet()) {
            String roleID = entry.getKey();
            Role role = entry.getValue();
            AbstractClaim claim = getClaim(source);
            GameProfile profile = getGameProfile(source, "player");
            if (profile != null && claim.getPermissionManager().getRemovedRoles(profile.getId()).contains(role)) names.add(roleID);
        }
        return CommandSource.suggestMatching(names, builder);
    };

    public static final SuggestionProvider<ServerCommandSource> ADDED_ROLES_PROVIDER = (source, builder) -> {
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Role> entry : ItsOursMod.INSTANCE.getRoleManager().entrySet()) {
            String roleID = entry.getKey();
            Role role = entry.getValue();
            AbstractClaim claim = getClaim(source);
            GameProfile profile = getGameProfile(source, "player");
            if (profile != null && claim.getPermissionManager().getRoles(profile.getId()).containsKey(role)) names.add(roleID);
        }
        return CommandSource.suggestMatching(names, builder);
    };

    public static final SuggestionProvider<ServerCommandSource> TO_BE_UNSET_ROLES_PROVIDER = (source, builder) -> {
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Role> entry : ItsOursMod.INSTANCE.getRoleManager().entrySet()) {
            String roleID = entry.getKey();
            Role role = entry.getValue();
            AbstractClaim claim = getClaim(source);
            GameProfile profile = getGameProfile(source, "player");
            if (profile != null && (claim.getPermissionManager().getRoles(profile.getId()).containsKey(role) || claim.getPermissionManager().getRoles(profile.getId()).containsKey(role))) names.add(roleID);
        }
        return CommandSource.suggestMatching(names, builder);
    };

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = ownClaimArgument();
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> weight = RequiredArgumentBuilder.argument("weight", IntegerArgumentType.integer(1));
            weight.executes(ctx -> addRole(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), StringArgumentType.getString(ctx, "name"), IntegerArgumentType.getInteger(ctx, "weight")));
            RequiredArgumentBuilder<ServerCommandSource, String> name = argument("name", word()).suggests(REMOVED_ROLES_PROVIDER);
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            LiteralArgumentBuilder<ServerCommandSource> add = LiteralArgumentBuilder.literal("add");
            name.then(weight);
            player.then(name);
            add.then(player);
            claim.then(add);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, String> name = argument("name", word()).suggests(ADDED_ROLES_PROVIDER);
            name.executes(ctx -> removeRole(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), StringArgumentType.getString(ctx, "name")));
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            LiteralArgumentBuilder<ServerCommandSource> remove = LiteralArgumentBuilder.literal("remove");
            player.then(name);
            remove.then(player);
            claim.then(remove);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, String> name = argument("name", word()).suggests(TO_BE_UNSET_ROLES_PROVIDER);
            name.executes(ctx -> unsetRole(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), StringArgumentType.getString(ctx, "name")));
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            LiteralArgumentBuilder<ServerCommandSource> unset = LiteralArgumentBuilder.literal("unset");
            player.then(name);
            unset.then(player);
            claim.then(unset);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            player.executes(ctx -> listRoles(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player")));
            LiteralArgumentBuilder<ServerCommandSource> list = LiteralArgumentBuilder.literal("list");
            list.then(player);
            claim.then(list);
        }
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("role");
        command.then(claim);
        literal.then(command);
    }

    public static int addRole(ServerCommandSource source, AbstractClaim claim, GameProfile target, String name, int weight) throws CommandSyntaxException {
        Role role = validateRole(name);
        boolean changed = claim.getPermissionManager().addRole(target.getId(), role, weight);
        ((ClaimPlayer) source.getPlayer()).sendMessage(changed ? Component.text("Added ").color(Color.YELLOW)
                .append(Component.text(name).color(Color.ORANGE)).append(Component.text(" to ").color(Color.YELLOW))
                .append(Component.text(target.getName()).color(Color.ORANGE)) :
                Component.text("Nothing changed, " + target.getName() + " already has " + name + " in " + claim.getName()).color(Color.RED));
        return 1;
    }

    public static int removeRole(ServerCommandSource source, AbstractClaim claim, GameProfile target, String name) throws CommandSyntaxException {
        Role role = validateRole(name);
        boolean changed = claim.getPermissionManager().removeRole(target.getId(), role);
        ((ClaimPlayer) source.getPlayer()).sendMessage(changed ? Component.text("Removed ").color(Color.YELLOW)
                .append(Component.text(name).color(Color.ORANGE)).append(Component.text(" from ").color(Color.YELLOW))
                .append(Component.text(target.getName()).color(Color.ORANGE)) :
                Component.text("Nothing changed, " + target.getName() + " already has " + name + " removed in " + claim.getName()).color(Color.RED));
        return 1;
    }

    public static int unsetRole(ServerCommandSource source, AbstractClaim claim, GameProfile target, String name) throws CommandSyntaxException {
        Role role = validateRole(name);
        boolean changed = claim.getPermissionManager().unsetRole(target.getId(), role);
        ((ClaimPlayer) source.getPlayer()).sendMessage(changed ? Component.text("Unset ").color(Color.YELLOW)
                .append(Component.text(name).color(Color.ORANGE)).append(Component.text(" from ").color(Color.YELLOW))
                .append(Component.text(target.getName()).color(Color.ORANGE)) :
                Component.text("Nothing changed, " + target.getName() + " didn't have " + name + " added or removed in " + claim.getName()).color(Color.RED));
        return 1;
    }

    public static int listRoles(ServerCommandSource source, AbstractClaim claim, GameProfile target) throws CommandSyntaxException {
        //Hover builder
        Object2IntMap<Role> addedRoles = claim.getPermissionManager().getPlayerRoleManager(target.getId()).getRoles();
        TextComponent.Builder hover = Component.text().content(addedRoles.size() > 0 ? "Added: " : "").color(Color.LIGHT_GREEN);
        for (Object2IntMap.Entry<Role> roleEntry : addedRoles.object2IntEntrySet()) {
            hover.append(Component.text(ItsOursMod.INSTANCE.getRoleManager().getRoleID(roleEntry.getKey()) + " ").color(Color.LIGHT_GREEN));
        }
        List<Role> removedRoles = claim.getPermissionManager().getPlayerRoleManager(target.getId()).getRemoved();
        hover.append(Component.text((removedRoles.size() > 0 ? "\nRemoved: " : "")).color(Color.RED));
        for (Role role : removedRoles) {
            hover.append(Component.text(ItsOursMod.INSTANCE.getRoleManager().getRoleID(role) + " ").color(Color.RED));
        }
        //Main message builder
        Object2IntMap<Role> roles = claim.getRoles(target.getId());
        TextComponent.Builder builder = Component.text().content(target.getName()).color(Color.ORANGE)
                .append(Component.text(" has ").color(Color.YELLOW))
                .append(Component.text(roles.size()).color(Color.ORANGE))
                .append(Component.text(" role" + (roles.size() != 1 ? "s" : "" )+ " in ").color(Color.YELLOW))
                .append(Component.text(claim.getName()).color(Color.ORANGE))
                .append(Component.text(":\n"));
        builder.style((style) -> style.hoverEvent(HoverEvent.showText(hover.build())));
        int i = 0;
        for (Object2IntMap.Entry<Role> entry : roles.object2IntEntrySet()) {
            builder.append(Component.text(ItsOursMod.INSTANCE.getRoleManager().getRoleID(entry.getKey())).color(Color.LIGHT_GREEN))
                    .append(Component.text(" (").color(Color.LIGHT_GRAY))
                    .append(Component.text(String.valueOf(entry.getIntValue())).color(Color.LIGHT_BLUE))
                    .append(Component.text(")").color(Color.LIGHT_GRAY))
            ;
            if (i != roles.size() - 1) builder.append(Component.text(", ").color(Color.LIGHT_GRAY));
            i++;
        }

        ((ClaimPlayer) source.getPlayer()).sendMessage(builder.build());
        return 1;
    }

    private static Role validateRole(String name) throws CommandSyntaxException {
        if (!ItsOursMod.INSTANCE.getRoleManager().containsKey(name))
            throw new SimpleCommandExceptionType(new LiteralText("There is no role with that name")).create();
        else return ItsOursMod.INSTANCE.getRoleManager().get(name);
    }

}
