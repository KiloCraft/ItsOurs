package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.util.Modify;
import me.drex.itsours.claim.permission.util.Value;
import me.drex.itsours.claim.roles.ClaimRoleManager;
import me.drex.itsours.claim.roles.Role;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.command.argument.PermissionArgument;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Map;

import static me.drex.message.api.LocalizedMessage.localized;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RolesCommand extends AbstractCommand {

    public static final RolesCommand INSTANCE = new RolesCommand();

    public static final CommandSyntaxException ALREADY_EXISTS = new SimpleCommandExceptionType(localized("text.itsours.commands.roles.create.alreadyExists")).create();
    public static final DynamicCommandExceptionType DOESNT_EXIST = new DynamicCommandExceptionType((roleId) -> localized("text.itsours.commands.roles.doesntExist", Map.of("role_id", Text.literal(roleId.toString()))));
    public static final SuggestionProvider<ServerCommandSource> ROLE_ARGUMENT = (context, builder) -> {
        AbstractClaim claim = ClaimArgument.getClaim(context);
        return CommandSource.suggestMatching(claim.getRoleManager().getRoleIds(), builder);
    };

    public RolesCommand() {
        super("roles");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal
            .then(
                ClaimArgument.ownClaims()
                    .then(
                        literal("create")
                            .then(
                                argument("role", StringArgumentType.word())
                                    .executes(ctx -> createRole(ctx.getSource(), ClaimArgument.getClaim(ctx), StringArgumentType.getString(ctx, "role")))
                            )
                    )
                    .then(
                        literal("delete")
                            .then(
                                argument("role", StringArgumentType.word()).suggests(ROLE_ARGUMENT)
                                    .executes(ctx -> deleteRole(ctx.getSource(), ClaimArgument.getClaim(ctx), StringArgumentType.getString(ctx, "role")))
                            )
                    ).then(
                        literal("permission").then(
                            argument("role", StringArgumentType.word()).suggests(ROLE_ARGUMENT)
                                .then(
                                    PermissionArgument.permission().then(
                                        PermissionArgument.value()
                                            .executes(ctx -> setRolePermission(ctx.getSource(), ClaimArgument.getClaim(ctx), StringArgumentType.getString(ctx, "role"), PermissionArgument.getPermission(ctx), PermissionArgument.getValue(ctx)))
                                    ).executes(ctx -> checkRolePermission(ctx.getSource(), ClaimArgument.getClaim(ctx), StringArgumentType.getString(ctx, "role"), PermissionArgument.getPermission(ctx)))
                                )
                        )
                    )
                    .then(
                        literal("list")
                            .executes(ctx -> listRoles(ctx.getSource(), ClaimArgument.getClaim(ctx)))
                    ).then(
                        literal("join").then(
                            argument("role", StringArgumentType.word()).suggests(ROLE_ARGUMENT).then(
                                argument("targets", GameProfileArgumentType.gameProfile()).executes(ctx -> joinRole(ctx.getSource(), ClaimArgument.getClaim(ctx), StringArgumentType.getString(ctx, "role"), GameProfileArgumentType.getProfileArgument(ctx, "targets")))
                            )
                        )
                    ).then(
                        literal("leave").then(
                            argument("role", StringArgumentType.word()).suggests(ROLE_ARGUMENT).then(
                                argument("targets", GameProfileArgumentType.gameProfile()).executes(ctx -> leaveRole(ctx.getSource(), ClaimArgument.getClaim(ctx), StringArgumentType.getString(ctx, "role"), GameProfileArgumentType.getProfileArgument(ctx, "targets")))
                            )
                        )
                    )
            );


    }

    public int createRole(ServerCommandSource src, AbstractClaim claim, String roleId) throws CommandSyntaxException {
        validatePermission(src, claim, PermissionManager.MODIFY, Modify.PERMISSION.node());
        ClaimRoleManager roleManager = claim.getRoleManager();
        Role role = roleManager.getRole(roleId);
        if (role != null) throw ALREADY_EXISTS;
        roleManager.createRole(roleId);
        src.sendFeedback(() -> localized("text.itsours.commands.roles.create", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(src.getServer()),
            Map.of("role_id", Text.literal(roleId))
        )), false);
        return 1;
    }

    public int deleteRole(ServerCommandSource src, AbstractClaim claim, String roleId) throws CommandSyntaxException {
        validatePermission(src, claim, PermissionManager.MODIFY, Modify.PERMISSION.node());
        ClaimRoleManager roleManager = claim.getRoleManager();
        Role role = roleManager.getRole(roleId);
        if (role == null) throw DOESNT_EXIST.create(roleId);
        if (roleManager.removeRole(roleId)) {
            src.sendFeedback(() -> localized("text.itsours.commands.roles.remove", PlaceholderUtil.mergePlaceholderMaps(
                claim.placeholders(src.getServer()),
                Map.of("role_id", Text.literal(roleId))
            )), false);
            return 1;
        } else {
            src.sendError(localized("text.itsours.commands.roles.remove.defaultRoles"));
            return 0;
        }
    }

    public int listRoles(ServerCommandSource src, AbstractClaim claim) throws CommandSyntaxException {
        validatePermission(src, claim, PermissionManager.MODIFY, Modify.PERMISSION.node());
        ClaimRoleManager roleManager = claim.getRoleManager();
        MutableText roles = PlaceholderUtil.list(roleManager.getRoleIds(), (roleId) -> Map.of(
            "role_id", Text.literal(roleId)
        ), "text.itsours.commands.roles.list.list");
        src.sendFeedback(() -> localized("text.itsours.commands.roles.list", PlaceholderUtil.mergePlaceholderMaps(
            Map.of("roles", roles),
            claim.placeholders(src.getServer())
        )), false);
        return roleManager.roles().size();
    }

    public int setRolePermission(ServerCommandSource src, AbstractClaim claim, String roleId, Permission permission, Value value) throws CommandSyntaxException {
        validatePermission(src, claim, PermissionManager.MODIFY, Modify.PERMISSION.node());
        ClaimRoleManager roleManager = claim.getRoleManager();
        Role role = roleManager.getRole(roleId);
        if (role == null) throw DOESNT_EXIST.create(roleId);
        role.permissions().set(permission, value);
        src.sendFeedback(() -> localized("text.itsours.commands.roles.set", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(src.getServer()),
            Map.of(
                "role_id", Text.literal(roleId),
                "permission", Text.literal(permission.asString()),
                "value", value.format()
            )
        )), false);
        return 1;

    }

    public int checkRolePermission(ServerCommandSource src, AbstractClaim claim, String roleId, Permission permission) throws CommandSyntaxException {
        validatePermission(src, claim, PermissionManager.MODIFY, Modify.PERMISSION.node());
        ClaimRoleManager roleManager = claim.getRoleManager();
        Role role = roleManager.getRole(roleId);
        if (role == null) throw DOESNT_EXIST.create(roleId);
        Value value = role.permissions().get(permission);
        src.sendFeedback(() -> localized("text.itsours.commands.roles.check", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(src.getServer()),
            Map.of(
                "role_id", Text.literal(roleId),
                "permission", Text.literal(permission.asString()),
                "value", value.format()
            )
        )), false);
        return 1;
    }

    public int joinRole(ServerCommandSource src, AbstractClaim claim, String roleId, Collection<GameProfile> targets) throws CommandSyntaxException {
        validatePermission(src, claim, PermissionManager.MODIFY, Modify.PERMISSION.node());
        ClaimRoleManager roleManager = claim.getRoleManager();
        Role role = roleManager.getRole(roleId);
        if (role == null) throw DOESNT_EXIST.create(roleId);
        int success = 0;
        for (GameProfile target : targets) {
            if (role.players().add(target.getId())) {
                src.sendFeedback(() -> localized("text.itsours.commands.roles.create", PlaceholderUtil.mergePlaceholderMaps(
                    claim.placeholders(src.getServer()),
                    PlaceholderUtil.gameProfile("target_", target),
                    Map.of("role_id", Text.literal(roleId))
                )), false);
                success++;
            }
        }
        return success;
    }

    public int leaveRole(ServerCommandSource src, AbstractClaim claim, String roleId, Collection<GameProfile> targets) throws CommandSyntaxException {
        validatePermission(src, claim, PermissionManager.MODIFY, Modify.PERMISSION.node());
        ClaimRoleManager roleManager = claim.getRoleManager();
        Role role = roleManager.getRole(roleId);
        if (role == null) throw DOESNT_EXIST.create(roleId);
        int success = 0;
        for (GameProfile target : targets) {
            if (role.players().remove(target.getId())) {
                src.sendFeedback(() -> localized("text.itsours.commands.roles.leave", PlaceholderUtil.mergePlaceholderMaps(
                    claim.placeholders(src.getServer()),
                    PlaceholderUtil.gameProfile("target_", target),
                    Map.of("role_id", Text.literal(roleId))
                )), false);
                success++;
            }
        }
        return success;
    }

}

