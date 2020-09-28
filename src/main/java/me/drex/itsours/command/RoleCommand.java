package me.drex.itsours.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.user.ClaimPlayer;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;


public class RoleCommand extends Command {

    public void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> claim = claimArgument();
        {
            //TODO: Add proper suggestion
            RequiredArgumentBuilder<ServerCommandSource, Integer> weight = RequiredArgumentBuilder.argument("weight", IntegerArgumentType.integer(1));
            weight.executes(ctx -> setRole(ctx.getSource(), getClaim(ctx), getGameProfile(ctx, "player"), StringArgumentType.getString(ctx, "name"), IntegerArgumentType.getInteger(ctx, "weight")));
            RequiredArgumentBuilder<ServerCommandSource, String> name = RequiredArgumentBuilder.argument("name", StringArgumentType.word());
            RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> player = RequiredArgumentBuilder.argument("player", GameProfileArgumentType.gameProfile());
            LiteralArgumentBuilder<ServerCommandSource> set = LiteralArgumentBuilder.literal("set");
            name.then(weight);
            player.then(name);
            set.then(player);
            claim.then(set);
        }
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("role");
        command.then(claim);
        literal.then(command);
    }

    public int setRole(ServerCommandSource source, AbstractClaim claim, GameProfile target, String name, int weight) throws CommandSyntaxException {
        if (!ItsOursMod.INSTANCE.getRoleManager().containsKey(name)) throw new SimpleCommandExceptionType(new LiteralText("There is no role with that name")).create();
        Role role = ItsOursMod.INSTANCE.getRoleManager().get(name);
        claim.getPermissionManager().addRole(target.getId(), role, weight);
        ((ClaimPlayer)source.getPlayer()).sendMessage(new LiteralText("Added ").formatted(Formatting.YELLOW)
                .append(new LiteralText(name).formatted(Formatting.GOLD)).append(new LiteralText(" to ").formatted(Formatting.YELLOW))
                .append(new LiteralText(target.getName()).formatted(Formatting.GOLD)));
        return 1;
    }

}
