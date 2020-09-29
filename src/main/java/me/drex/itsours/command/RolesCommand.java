package me.drex.itsours.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import static me.drex.itsours.util.TextUtil.format;

public class RolesCommand extends Command {

    public void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("roles");

        {
            RequiredArgumentBuilder<ServerCommandSource, String> name = RequiredArgumentBuilder.argument("name", StringArgumentType.word());
            name.executes(ctx -> addRole(ctx.getSource(), StringArgumentType.getString(ctx, "name")));
            LiteralArgumentBuilder<ServerCommandSource> add = LiteralArgumentBuilder.literal("add");
            add.then(name);
            command.then(add);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, String> name = roleArgument();
            name.executes(ctx -> removeRole(ctx.getSource(), StringArgumentType.getString(ctx, "name")));
            LiteralArgumentBuilder<ServerCommandSource> remove = LiteralArgumentBuilder.literal("remove");
            remove.then(name);
            command.then(remove);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, Boolean> value = RequiredArgumentBuilder.argument("value", BoolArgumentType.bool());
            value.executes(ctx -> setPermission(ctx.getSource(), StringArgumentType.getString(ctx, "name"), StringArgumentType.getString(ctx, "perm"), BoolArgumentType.getBool(ctx, "value")));
            RequiredArgumentBuilder<ServerCommandSource, String> perm = RequiredArgumentBuilder.argument("perm", StringArgumentType.word());
            RequiredArgumentBuilder<ServerCommandSource, String> name = roleArgument();
            name.executes(ctx -> listPermission(ctx.getSource(), StringArgumentType.getString(ctx, "name")));
            LiteralArgumentBuilder<ServerCommandSource> permission = LiteralArgumentBuilder.literal("permission");
            perm.then(value);
            name.then(perm);
            permission.then(name);
            command.then(permission);
        }

        literal.then(command);
    }

    public int addRole(ServerCommandSource source, String name) throws CommandSyntaxException {
        if (ItsOursMod.INSTANCE.getRoleManager().containsKey(name)) throw new SimpleCommandExceptionType(new LiteralText("A role with that name already exists")).create();
        ItsOursMod.INSTANCE.getRoleManager().put(name, new Role(new CompoundTag()));
        ((ClaimPlayer)source.getPlayer()).sendMessage(new LiteralText("Role ").formatted(Formatting.YELLOW).append(new LiteralText(name).formatted(Formatting.GOLD).append(new LiteralText(" has been added").formatted(Formatting.YELLOW))));

        return 1;
    }

    public int removeRole(ServerCommandSource source, String name) throws CommandSyntaxException {
        if (!ItsOursMod.INSTANCE.getRoleManager().containsKey(name)) throw new SimpleCommandExceptionType(new LiteralText("There is no role with that name")).create();
        ItsOursMod.INSTANCE.getRoleManager().remove(name);
        ((ClaimPlayer)source.getPlayer()).sendMessage(new LiteralText("Role ").formatted(Formatting.YELLOW).append(new LiteralText(name).formatted(Formatting.GOLD).append(new LiteralText(" has been removed").formatted(Formatting.YELLOW))));
        return 1;
    }

    public int setPermission(ServerCommandSource source, String name, String permission, boolean value) throws CommandSyntaxException {
        if (!ItsOursMod.INSTANCE.getRoleManager().containsKey(name)) throw new SimpleCommandExceptionType(new LiteralText("There is no role with that name")).create();
        Role role = ItsOursMod.INSTANCE.getRoleManager().get(name);
        role.permissions().setPermission(permission, value);
        ((ClaimPlayer)source.getPlayer()).sendMessage(new LiteralText("Set ").formatted(Formatting.YELLOW)
                .append(new LiteralText(permission).formatted(Formatting.GOLD)).append(new LiteralText(" for ").formatted(Formatting.YELLOW))
                .append(new LiteralText(name).formatted(Formatting.GOLD)
                .append(new LiteralText(" to ").formatted(Formatting.YELLOW)).append(format(value))));
        return 1;
    }

    public int listPermission(ServerCommandSource source, String name) throws CommandSyntaxException {
        if (!ItsOursMod.INSTANCE.getRoleManager().containsKey(name)) throw new SimpleCommandExceptionType(new LiteralText("There is no role with that name")).create();
        Role role = ItsOursMod.INSTANCE.getRoleManager().get(name);
        ((ClaimPlayer)source.getPlayer()).sendMessage(new LiteralText(name).formatted(Formatting.YELLOW).append("\n").append(role.permissions().toText()));
        return 1;
    }


}
