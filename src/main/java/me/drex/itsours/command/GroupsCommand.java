package me.drex.itsours.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.util.Modify;
import me.drex.itsours.claim.flags.util.Value;
import me.drex.itsours.claim.groups.ClaimGroupManager;
import me.drex.itsours.claim.groups.Group;
import me.drex.itsours.command.argument.ClaimArgument;
import me.drex.itsours.command.argument.FlagArgument;
import me.drex.itsours.gui.players.GroupManagerGui;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.players.NameAndId;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.Map;

import static me.drex.itsours.util.LocalizedMessage.localized;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class GroupsCommand extends AbstractCommand {

    public static final GroupsCommand INSTANCE = new GroupsCommand();

    public static final CommandSyntaxException ALREADY_EXISTS = new SimpleCommandExceptionType(localized("text.itsours.commands.groups.create.alreadyExists")).create();
    public static final DynamicCommandExceptionType DOESNT_EXIST = new DynamicCommandExceptionType((groupId) -> localized("text.itsours.commands.groups.doesntExist", Map.of("group_id", Component.literal(groupId.toString()))));
    public static final SuggestionProvider<CommandSourceStack> GROUP_ARGUMENT = (context, builder) -> {
        AbstractClaim claim = ClaimArgument.getClaim(context);
        return SharedSuggestionProvider.suggest(claim.getGroupManager().getGroupIds(), builder);
    };

    public GroupsCommand() {
        super("groups");
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        literal
            .then(
                ClaimArgument.ownClaims()
                    .then(
                        literal("create")
                            .then(
                                argument("group", StringArgumentType.word())
                                    .executes(ctx -> createGroup(ctx.getSource(), ClaimArgument.getClaim(ctx), StringArgumentType.getString(ctx, "group")))
                            )
                    )
                    .then(
                        literal("delete")
                            .then(
                                argument("group", StringArgumentType.word()).suggests(GROUP_ARGUMENT)
                                    .executes(ctx -> deleteGroup(ctx.getSource(), ClaimArgument.getClaim(ctx), StringArgumentType.getString(ctx, "group")))
                            )
                    ).then(
                        literal("flags").then(
                            argument("group", StringArgumentType.word()).suggests(GROUP_ARGUMENT)
                                .then(
                                    FlagArgument.playerFlag().then(
                                        FlagArgument.value()
                                            .executes(ctx -> setGroupFlags(ctx.getSource(), ClaimArgument.getClaim(ctx), StringArgumentType.getString(ctx, "group"), FlagArgument.getPlayerFlag(ctx), FlagArgument.getValue(ctx)))
                                    ).executes(ctx -> checkGroupFlags(ctx.getSource(), ClaimArgument.getClaim(ctx), StringArgumentType.getString(ctx, "group"), FlagArgument.getPlayerFlag(ctx)))
                                )
                        )
                    )
                    .then(
                        literal("list")
                            .executes(ctx -> listGroups(ctx.getSource(), ClaimArgument.getClaim(ctx)))
                    ).then(
                        literal("join").then(
                            argument("group", StringArgumentType.word()).suggests(GROUP_ARGUMENT).then(
                                argument("targets", GameProfileArgument.gameProfile()).executes(ctx -> joinGroup(ctx.getSource(), ClaimArgument.getClaim(ctx), StringArgumentType.getString(ctx, "group"), GameProfileArgument.getGameProfiles(ctx, "targets")))
                            )
                        )
                    ).then(
                        literal("leave").then(
                            argument("group", StringArgumentType.word()).suggests(GROUP_ARGUMENT).then(
                                argument("targets", GameProfileArgument.gameProfile()).executes(ctx -> leaveGroup(ctx.getSource(), ClaimArgument.getClaim(ctx), StringArgumentType.getString(ctx, "group"), GameProfileArgument.getGameProfiles(ctx, "targets")))
                            )
                        )
                    ).executes(context -> {
                        new GroupManagerGui(new GuiContext(context.getSource().getPlayerOrException()), ClaimArgument.getClaim(context)).open();
                        return 1;
                    })
            );


    }

    public int createGroup(CommandSourceStack src, AbstractClaim claim, String groupId) throws CommandSyntaxException {
        validateAction(src, claim, Flags.MODIFY, Modify.FLAG.node());
        ClaimGroupManager groupManager = claim.getGroupManager();
        Group group = groupManager.getGroup(groupId);
        if (group != null) throw ALREADY_EXISTS;
        groupManager.createGroup(groupId);
        src.sendSuccess(() -> localized("text.itsours.commands.groups.create", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(src.getServer()),
            Map.of("group_id", Component.literal(groupId))
        )), false);
        return 1;
    }

    public int deleteGroup(CommandSourceStack src, AbstractClaim claim, String groupId) throws CommandSyntaxException {
        validateAction(src, claim, Flags.MODIFY, Modify.FLAG.node());
        ClaimGroupManager groupManager = claim.getGroupManager();
        Group group = groupManager.getGroup(groupId);
        if (group == null) throw DOESNT_EXIST.create(groupId);
        if (groupManager.deleteGroup(groupId)) {
            src.sendSuccess(() -> localized("text.itsours.commands.groups.remove", PlaceholderUtil.mergePlaceholderMaps(
                claim.placeholders(src.getServer()),
                Map.of("group_id", Component.literal(groupId))
            )), false);
            return 1;
        } else {
            src.sendFailure(localized("text.itsours.commands.groups.remove.defaultGroups"));
            return 0;
        }
    }

    public int listGroups(CommandSourceStack src, AbstractClaim claim) throws CommandSyntaxException {
        validateAction(src, claim, Flags.MODIFY, Modify.FLAG.node());
        ClaimGroupManager groupManager = claim.getGroupManager();
        MutableComponent groups = PlaceholderUtil.list(groupManager.getGroupIds(), (groupId) -> Map.of(
            "group_id", Component.literal(groupId)
        ), "text.itsours.commands.groups.list.list");
        src.sendSuccess(() -> localized("text.itsours.commands.groups.list", PlaceholderUtil.mergePlaceholderMaps(
            Map.of("groups", groups),
            claim.placeholders(src.getServer())
        )), false);
        return groupManager.groups().size();
    }

    public int setGroupFlags(CommandSourceStack src, AbstractClaim claim, String groupId, Flag flag, Value value) throws CommandSyntaxException {
        validateAction(src, claim, Flags.MODIFY, Modify.FLAG.node());
        ClaimGroupManager groupManager = claim.getGroupManager();
        Group group = groupManager.getGroup(groupId);
        if (group == null) throw DOESNT_EXIST.create(groupId);
        group.flags().set(flag, value);
        src.sendSuccess(() -> localized("text.itsours.commands.groups.set", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(src.getServer()),
            Map.of(
                "group_id", Component.literal(groupId),
                "flag", Component.literal(flag.asString()),
                "value", value.format()
            )
        )), false);
        return 1;

    }

    public int checkGroupFlags(CommandSourceStack src, AbstractClaim claim, String groupId, Flag flag) throws CommandSyntaxException {
        validateAction(src, claim, Flags.MODIFY, Modify.FLAG.node());
        ClaimGroupManager groupManager = claim.getGroupManager();
        Group group = groupManager.getGroup(groupId);
        if (group == null) throw DOESNT_EXIST.create(groupId);
        Value value = group.flags().get(flag);
        src.sendSuccess(() -> localized("text.itsours.commands.groups.check", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(src.getServer()),
            Map.of(
                "group_id", Component.literal(groupId),
                "flag", Component.literal(flag.asString()),
                "value", value.format()
            )
        )), false);
        return 1;
    }

    public int joinGroup(CommandSourceStack src, AbstractClaim claim, String groupId, Collection<NameAndId> targets) throws CommandSyntaxException {
        validateAction(src, claim, Flags.MODIFY, Modify.FLAG.node());
        ClaimGroupManager groupManager = claim.getGroupManager();
        Group group = groupManager.getGroup(groupId);
        if (group == null) throw DOESNT_EXIST.create(groupId);
        int success = 0;
        for (NameAndId target : targets) {
            if (group.players().add(target.id())) {
                src.sendSuccess(() -> localized("text.itsours.commands.groups.create", PlaceholderUtil.mergePlaceholderMaps(
                    claim.placeholders(src.getServer()),
                    PlaceholderUtil.gameProfile("target_", target),
                    Map.of("group_id", Component.literal(groupId))
                )), false);
                success++;
            }
        }
        return success;
    }

    public int leaveGroup(CommandSourceStack src, AbstractClaim claim, String groupId, Collection<NameAndId> targets) throws CommandSyntaxException {
        validateAction(src, claim, Flags.MODIFY, Modify.FLAG.node());
        ClaimGroupManager groupManager = claim.getGroupManager();
        Group group = groupManager.getGroup(groupId);
        if (group == null) throw DOESNT_EXIST.create(groupId);
        int success = 0;
        for (NameAndId target : targets) {
            if (group.players().remove(target.id())) {
                src.sendSuccess(() -> localized("text.itsours.commands.groups.leave", PlaceholderUtil.mergePlaceholderMaps(
                    claim.placeholders(src.getServer()),
                    PlaceholderUtil.gameProfile("target_", target),
                    Map.of("group_id", Component.literal(groupId))
                )), false);
                success++;
            }
        }
        return success;
    }

}
