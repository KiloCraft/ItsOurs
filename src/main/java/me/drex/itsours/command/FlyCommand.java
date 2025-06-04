package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.user.PlayerData;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.Optional;

public class FlyCommand extends ToggleCommand {

    public static final FlyCommand INSTANCE = new FlyCommand();

    private FlyCommand() {
        super("fly", PlayerData::flight, PlayerData::setFlight, "text.itsours.commands.fly");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        super.register(literal.requires(src -> ItsOurs.checkPermission(src, "itsours.fly", 2)));
    }

    @Override
    protected void afterToggle(ServerCommandSource src, boolean newValue) throws CommandSyntaxException {
        ServerPlayerEntity player = src.getPlayerOrThrow();
        Optional<AbstractClaim> optional = ClaimList.getClaimAt(player);
        if (optional.isPresent() && optional.get().checkAction(player.getUuid(), Flags.CLAIM_FLY)) {
            player.interactionManager.getGameMode().setAbilities(player.getAbilities());
            if (newValue) {
                player.getAbilities().allowFlying = true;
            }
            player.sendAbilitiesUpdate();
        }
        super.afterToggle(src, newValue);
    }
}
