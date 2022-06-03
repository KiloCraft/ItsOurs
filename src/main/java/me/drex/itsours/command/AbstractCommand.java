package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCommand {

    public static final CommandSyntaxException NO_CLAIM_AT_POS = new SimpleCommandExceptionType(Text.translatable("text.itsours.argument.claim.noClaimAtPos")).create();
    public static final CommandSyntaxException NO_PERMISSION = new SimpleCommandExceptionType(Text.translatable("text.itsours.argument.general.noPermission")).create();

    protected final String literal;

    public AbstractCommand(@NotNull String literal) {
        this.literal = literal;
    }

    public void registerCommand(LiteralArgumentBuilder<ServerCommandSource> literal) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgument = CommandManager.literal(this.literal);
        register(literalArgument);
        literal.then(literalArgument);
    }

    protected abstract void register(LiteralArgumentBuilder<ServerCommandSource> literal);

    public String getLiteral() {
        return literal;
    }

    public AbstractClaim getClaim(Entity entity) throws CommandSyntaxException {
        return ClaimList.INSTANCE.getClaimAt(entity).orElseThrow(() -> NO_CLAIM_AT_POS);
    }

    public void validatePermission(ServerCommandSource src, AbstractClaim claim, Node... nodes) throws CommandSyntaxException {
        // Console
        if (src.getEntity() == null) return;
        // Throw exception if player doesn't have requested permissions
        if (!claim.hasPermission(src.getPlayer().getUuid(), nodes)) throw NO_PERMISSION;
    }

}
