package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.node.ChildNode;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import static me.drex.itsours.util.LocalizedMessage.localized;

public abstract class AbstractCommand {

    public static final DynamicCommandExceptionType NO_CLAIM_AT_POS = new DynamicCommandExceptionType(source -> localized("text.itsours.argument.claim.notFoundPos", (CommandSourceStack) source));
    public static final DynamicCommandExceptionType MISSING_PERMISSION = new DynamicCommandExceptionType(source -> localized("text.itsours.argument.general.missingPermission", (CommandSourceStack) source));

    protected final String literal;

    public AbstractCommand(@NotNull String literal) {
        this.literal = literal;
    }

    public void registerCommand(LiteralArgumentBuilder<CommandSourceStack> literal) {
        LiteralArgumentBuilder<CommandSourceStack> literalArgument = Commands.literal(this.literal);
        register(literalArgument);
        literal.then(literalArgument);
    }

    protected abstract void register(LiteralArgumentBuilder<CommandSourceStack> literal);

    public String getLiteral() {
        return literal;
    }

    public AbstractClaim getClaim(CommandSourceStack source) throws CommandSyntaxException {
        return ClaimList.getClaimAt(source.getLevel(), BlockPos.containing(source.getPosition())).orElseThrow(() -> NO_CLAIM_AT_POS.create(source));
    }

    public void validateAction(CommandSourceStack src, AbstractClaim claim, ChildNode... nodes) throws CommandSyntaxException {
        // Console
        if (src.getEntity() == null) return;
        // Throw exception if player doesn't have requested permissions
        if (!claim.checkAction(src.getPlayerOrException().getUUID(), nodes)) throw MISSING_PERMISSION.create(src);
    }

}
