package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;


public class RoleCommand extends Command {


    //TODO: Check if the executor is allowed to do this
    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = claimArgument();
        {
            RequiredArgumentBuilder<ServerCommandSource, Integer> weight = RequiredArgumentBuilder.argument("weight", IntegerArgumentType.integer(1));
            weight.executes(ctx -> addRole(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), StringArgumentType.getString(ctx, "name"), IntegerArgumentType.getInteger(ctx, "weight")));
            RequiredArgumentBuilder<ServerCommandSource, String> name = roleArgument();
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            LiteralArgumentBuilder<ServerCommandSource> add = LiteralArgumentBuilder.literal("add");
            name.then(weight);
            player.then(name);
            add.then(player);
            claim.then(add);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, String> name = argument("name", word()).suggests(OWNED_ROLES_PROVIDER);
            name.executes(ctx -> removeRole(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), StringArgumentType.getString(ctx, "name")));
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            LiteralArgumentBuilder<ServerCommandSource> remove = LiteralArgumentBuilder.literal("remove");
            player.then(name);
            remove.then(player);
            claim.then(remove);
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
        if (!ItsOursMod.INSTANCE.getRoleManager().containsKey(name)) throw new SimpleCommandExceptionType(new LiteralText("There is no role with that name")).create();
        Role role = ItsOursMod.INSTANCE.getRoleManager().get(name);
        claim.getPermissionManager().addRole(target.getId(), role, weight);
        ((ClaimPlayer)source.getPlayer()).sendMessage(Component.text("Added ").color(Color.YELLOW)
                .append(Component.text(name).color(Color.ORANGE)).append(Component.text(" to ").color(Color.YELLOW))
                .append(Component.text(target.getName()).color(Color.ORANGE)));
        return 1;
    }

    public static int removeRole(ServerCommandSource source, AbstractClaim claim, GameProfile target, String name) throws CommandSyntaxException {
        if (!ItsOursMod.INSTANCE.getRoleManager().containsKey(name)) throw new SimpleCommandExceptionType(new LiteralText("There is no role with that name")).create();
        Role role = ItsOursMod.INSTANCE.getRoleManager().get(name);
        if (!claim.getPermissionManager().hasRole(target.getId(), role)) throw new SimpleCommandExceptionType(new LiteralText(target.getName() + " doesn't have a role with that name")).create();
        claim.getPermissionManager().removeRole(target.getId(), role);
        ((ClaimPlayer)source.getPlayer()).sendMessage(Component.text("Removed ").color(Color.YELLOW)
                .append(Component.text(name).color(Color.ORANGE)).append(Component.text(" from ").color(Color.YELLOW))
                .append(Component.text(target.getName()).color(Color.ORANGE)));
        return 1;
    }

    public static int listRoles(ServerCommandSource source, AbstractClaim claim, GameProfile target) throws CommandSyntaxException {
        TextComponent.Builder builder = Component.text().content("Roles (").color(Color.YELLOW).append(Component.text(target.getName()).color(Color.ORANGE).append(Component.text("):\n").color(Color.YELLOW)));
        PermissionManager pm = claim.getPermissionManager();
        for (Map.Entry<Role, Integer> entry : pm.getRolesByWeight(target.getId()).entrySet()) {
            builder.append(Component.text(ItsOursMod.INSTANCE.getRoleManager().getRoleID(entry.getKey()) + " (").color(Color.YELLOW))
            .append(Component.text(String.valueOf(entry.getValue())).color(Color.ORANGE))
            .append(Component.text(") ").color(Color.YELLOW));
        }
        ((ClaimPlayer)source.getPlayer()).sendMessage(builder.build());
        return 1;
    }

    public static final SuggestionProvider<ServerCommandSource> OWNED_ROLES_PROVIDER = (source, builder) -> {
        List<String> names = new ArrayList<>();
        for (Map.Entry<String, Role> entry : ItsOursMod.INSTANCE.getRoleManager().entrySet()) {
            String roleID = entry.getKey();
            Role role = entry.getValue();
            AbstractClaim claim = getClaim(source);
            GameProfile profile = getGameProfile(source, "player");
            if (claim != null && profile != null && claim.getPermissionManager().hasRole(profile.getId(), role)) names.add(roleID);
        }
        return CommandSource.suggestMatching(names, builder);
    };

}
