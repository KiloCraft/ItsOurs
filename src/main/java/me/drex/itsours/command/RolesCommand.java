package me.drex.itsours.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.roles.Role;
import net.minecraft.text.Text;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;


public class RolesCommand extends Command {

    private static final SimpleCommandExceptionType ALREADY_EXCISTS = new SimpleCommandExceptionType(Text.translatable("text.itsours.command.roles.already_exists"));
    private static final SimpleCommandExceptionType UNKNOWN_ROLE = new SimpleCommandExceptionType(Text.translatable("text.itsours.command.roles.unknown"));
    private static final SimpleCommandExceptionType CANT_REMOVE = new SimpleCommandExceptionType(Text.translatable("text.itsours.command.roles.cant_remove"));

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("roles");
        {
            RequiredArgumentBuilder<ServerCommandSource, String> name = RequiredArgumentBuilder.argument("name", StringArgumentType.word());
            name.executes(ctx -> addRole(ctx.getSource(), StringArgumentType.getString(ctx, "name")));
            LiteralArgumentBuilder<ServerCommandSource> add = LiteralArgumentBuilder.literal("add");
            add.requires(src -> hasPermission(src, "itsours.roles.add"));
            add.then(name);
            command.then(add);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, String> name = roleArgument();
            name.executes(ctx -> removeRole(ctx.getSource(), StringArgumentType.getString(ctx, "name")));
            LiteralArgumentBuilder<ServerCommandSource> remove = LiteralArgumentBuilder.literal("remove");
            remove.requires(src -> hasPermission(src, "itsours.roles.remove"));
            remove.then(name);
            command.then(remove);
        }
        {
            RequiredArgumentBuilder<ServerCommandSource, String> value = permissionValueArgument();
            value.executes(ctx -> setPermission(ctx.getSource(), StringArgumentType.getString(ctx, "name"), getPermission(ctx), getPermissionValue(ctx)));
            RequiredArgumentBuilder<ServerCommandSource, String> perm = permissionArgument();
            RequiredArgumentBuilder<ServerCommandSource, String> name = roleArgument();
            name.executes(ctx -> listPermission(ctx.getSource(), StringArgumentType.getString(ctx, "name")));
            LiteralArgumentBuilder<ServerCommandSource> permission = LiteralArgumentBuilder.literal("permission");
            permission.requires(src -> hasPermission(src, "itsours.roles.permission"));
            perm.then(value);
            name.then(perm);
            permission.then(name);
            command.then(permission);
        }

        literal.then(command);
    }

    public static int addRole(ServerCommandSource source, String name) throws CommandSyntaxException {
        if (ItsOurs.INSTANCE.getRoleManager().containsKey(name))
            throw ALREADY_EXCISTS.create();
        ItsOurs.INSTANCE.getRoleManager().put(name, new Role(new NbtCompound()));
        source.sendFeedback(Text.translatable("text.itsours.command.roles.add", name), false);
        return 1;
    }

    public static int removeRole(ServerCommandSource source, String name) throws CommandSyntaxException {
        if (!ItsOurs.INSTANCE.getRoleManager().containsKey(name))
            throw UNKNOWN_ROLE.create();
        if (name.equals("default") || name.equals("trusted"))
            throw CANT_REMOVE.create();
        ItsOurs.INSTANCE.getRoleManager().remove(name);
        source.sendFeedback(Text.translatable("text.itsours.command.roles.remove", name), false);
        return 1;
    }

    public static int setPermission(ServerCommandSource source, String name, Permission permission, Permission.Value value) throws CommandSyntaxException {
        if (!ItsOurs.INSTANCE.getRoleManager().containsKey(name))
            throw UNKNOWN_ROLE.create();
        Role role = ItsOurs.INSTANCE.getRoleManager().get(name);
        role.permissions().setPermission(permission.asString(), value);
        source.sendFeedback(Text.translatable("text.itsours.command.roles.set_permission", permission.asString(), name, value).formatted(Formatting.GREEN), false);
        return 1;
    }

    public static int listPermission(ServerCommandSource source, String name) throws CommandSyntaxException {
        if (!ItsOurs.INSTANCE.getRoleManager().containsKey(name))
            throw UNKNOWN_ROLE.create();
        Role role = ItsOurs.INSTANCE.getRoleManager().get(name);
        source.sendFeedback(Text.translatable("text.itsours.command.roles.list_permission", name, role.permissions().toText()), false);
        return 1;
    }

}
