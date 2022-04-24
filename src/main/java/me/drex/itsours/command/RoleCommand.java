package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.roles.Role;
import net.minecraft.text.Text;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;


public class RoleCommand extends Command {

    public static final SuggestionProvider<ServerCommandSource> REMOVED_ROLES_PROVIDER = (source, builder) -> {
        List<String> names = new ArrayList<>();
        getGameProfile(source, "player", profile -> {
            for (Map.Entry<String, Role> entry : ItsOurs.INSTANCE.getRoleManager().entrySet()) {
                String roleID = entry.getKey();
                Role role = entry.getValue();
                AbstractClaim claim = getClaim(source);
                if (claim.getPermissionManager().getRemovedRoles(profile.getId()).contains(role)) names.add(roleID);
            }
        });
        return CommandSource.suggestMatching(names, builder);
    };

    public static final SuggestionProvider<ServerCommandSource> ADDED_ROLES_PROVIDER = (source, builder) -> {
        List<String> names = new ArrayList<>();
        getGameProfile(source, "player", profile -> {
            for (Map.Entry<String, Role> entry : ItsOurs.INSTANCE.getRoleManager().entrySet()) {
                String roleID = entry.getKey();
                Role role = entry.getValue();
                AbstractClaim claim = getClaim(source);
                if (claim.getPermissionManager().getRoles(profile.getId()).containsKey(role)) names.add(roleID);
            }
        });
        return CommandSource.suggestMatching(names, builder);
    };

    public static final SuggestionProvider<ServerCommandSource> TO_BE_UNSET_ROLES_PROVIDER = (source, builder) -> {
        List<String> names = new ArrayList<>();
        getGameProfile(source, "player", profile -> {
            for (Map.Entry<String, Role> entry : ItsOurs.INSTANCE.getRoleManager().entrySet()) {
                String roleID = entry.getKey();
                Role role = entry.getValue();
                AbstractClaim claim = getClaim(source);
                if ((claim.getPermissionManager().getRoles(profile.getId()).containsKey(role) || claim.getPermissionManager().getRoles(profile.getId()).containsKey(role)))
                    names.add(roleID);
            }
        });
        return CommandSource.suggestMatching(names, builder);
    };

    // TODO: Roles should only be on / off, not 3-state
    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = ownClaimArgument();
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> weight = RequiredArgumentBuilder.argument("weight", IntegerArgumentType.integer(1));
            weight.executes(RoleCommand::addRole);
            RequiredArgumentBuilder<ServerCommandSource, String> name = argument("name", word()).suggests(REMOVED_ROLES_PROVIDER);
            RequiredArgumentBuilder<ServerCommandSource, String> player = playerArgument("player");
            LiteralArgumentBuilder<ServerCommandSource> add = LiteralArgumentBuilder.literal("add");
            name.then(weight);
            player.then(name);
            add.then(player);
            claim.then(add);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, String> name = argument("name", word()).suggests(ADDED_ROLES_PROVIDER);
            name.executes(RoleCommand::removeRole);
            RequiredArgumentBuilder<ServerCommandSource, String> player = playerArgument("player");
            LiteralArgumentBuilder<ServerCommandSource> remove = LiteralArgumentBuilder.literal("remove");
            player.then(name);
            remove.then(player);
            claim.then(remove);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, String> name = argument("name", word()).suggests(TO_BE_UNSET_ROLES_PROVIDER);
            name.executes(RoleCommand::unsetRole);
            RequiredArgumentBuilder<ServerCommandSource, String> player = playerArgument("player");
            LiteralArgumentBuilder<ServerCommandSource> unset = LiteralArgumentBuilder.literal("unset");
            player.then(name);
            unset.then(player);
            claim.then(unset);
        }
        // TODO:
        /*{
            RequiredArgumentBuilder<ServerCommandSource, String> player = playerArgument("player");
            player.executes(RoleCommand::listRoles);
            LiteralArgumentBuilder<ServerCommandSource> list = LiteralArgumentBuilder.literal("list");
            list.then(player);
            claim.then(list);
        }*/
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("role");
        command.then(claim);
        literal.then(command);
    }

    public static int addRole(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        AbstractClaim claim = getClaim(ctx);
        String name = StringArgumentType.getString(ctx, "name");
        int weight = IntegerArgumentType.getInteger(ctx, "weight");
        getGameProfile(ctx, "player", profile -> addRole(source, claim, profile, name, weight));
        return 1;
    }

    public static int addRole(ServerCommandSource source, AbstractClaim claim, GameProfile profile, String name, int weight) throws CommandSyntaxException {
        validatePermission(claim, source, "modify.role");
        Role role = validateRole(name);
        boolean changed = claim.getPermissionManager().addRole(profile.getId(), role, weight);
        if (changed) {
            source.sendFeedback(Text.translatable("text.itsours.command.role.add", name, profile), false);
        } else {
            source.sendError(Text.translatable("text.itsours.command.role.no_change"));
        }
        return 1;
    }

    public static int removeRole(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        AbstractClaim claim = getClaim(ctx);
        String name = StringArgumentType.getString(ctx, "name");
        getGameProfile(ctx, "player", profile -> removeRole(source, claim, profile, name));
        return 1;
    }

    public static int removeRole(ServerCommandSource source, AbstractClaim claim, GameProfile profile, String name) throws CommandSyntaxException {
        validatePermission(claim, source, "modify.role");
        Role role = validateRole(name);
        boolean changed = claim.getPermissionManager().removeRole(profile.getId(), role);
        if (changed) {
            source.sendFeedback(Text.translatable("text.itsours.command.role.remove", name, profile), false);
        } else {
            source.sendError(Text.translatable("text.itsours.command.role.no_change"));
        }
        return 1;
    }

    public static int unsetRole(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        AbstractClaim claim = getClaim(ctx);
        String name = StringArgumentType.getString(ctx, "name");
        getGameProfile(ctx, "player", profile -> unsetRole(source, claim, profile, name));
        return 1;
    }

    public static int unsetRole(ServerCommandSource source, AbstractClaim claim, GameProfile profile, String name) throws CommandSyntaxException {
        validatePermission(claim, source, "modify.role");
        Role role = validateRole(name);
        boolean changed = claim.getPermissionManager().unsetRole(profile.getId(), role);
        if (changed) {
            source.sendFeedback(Text.translatable("text.itsours.command.role.unset", name, profile), false);
        } else {
            source.sendError(Text.translatable("text.itsours.command.role.no_change"));
        }
        return 1;
    }

    /*public static int listRoles(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        AbstractClaim claim = getClaim(ctx);
        validatePermission(claim, source, "modify.role");
        getGameProfile(ctx, "player", profile -> {
            //Hover builder
            Object2IntMap<Role> addedRoles = claim.getPermissionManager().getPlayerRoleManager(profile.getId()).getRoles();
            TextComponent.Builder hover = Component.text().content(addedRoles.size() > 0 ? "Added: " : "").color(Color.LIGHT_GREEN);
            for (Object2IntMap.Entry<Role> roleEntry : addedRoles.object2IntEntrySet()) {
                hover.append(Component.text(ItsOursMod.INSTANCE.getRoleManager().getRoleID(roleEntry.getKey()) + " ").color(Color.LIGHT_GREEN));
            }
            List<Role> removedRoles = claim.getPermissionManager().getPlayerRoleManager(profile.getId()).getRemoved();
            hover.append(Component.text((removedRoles.size() > 0 ? "\nRemoved: " : "")).color(Color.RED));
            for (Role role : removedRoles) {
                hover.append(Component.text(ItsOursMod.INSTANCE.getRoleManager().getRoleID(role) + " ").color(Color.RED));
            }
            //Main message builder
            Object2IntMap<Role> roles = claim.getRoles(profile.getId());
            TextComponent.Builder builder = Component.text().content(profile.getName()).color(Color.ORANGE)
                    .append(Component.text(" has ").color(Color.YELLOW))
                    .append(Component.text(roles.size()).color(Color.ORANGE))
                    .append(Component.text(" role" + (roles.size() != 1 ? "s" : "") + " in ").color(Color.YELLOW))
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
        });

        return 1;
    }*/

    private static Role validateRole(String name) throws CommandSyntaxException {
        if (!ItsOurs.INSTANCE.getRoleManager().containsKey(name))
            throw new SimpleCommandExceptionType(Text.translatable("text.itsours.commands.exception.role.no_roles")).create();
        else return ItsOurs.INSTANCE.getRoleManager().get(name);
    }

}
