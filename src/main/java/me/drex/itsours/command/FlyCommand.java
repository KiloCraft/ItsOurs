package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.user.PlayerData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class FlyCommand extends ToggleCommand {

    public static final FlyCommand INSTANCE = new FlyCommand();

    private FlyCommand() {
        super("fly", PlayerData::flight, PlayerData::setFlight, "text.itsours.commands.fly");
    }

    @Override
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        super.register(literal.requires(src -> ItsOurs.checkPermission(src, "itsours.fly", 2)));
    }

    @Override
    protected void afterToggle(CommandSourceStack src, boolean newValue) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayerOrException();
        Optional<AbstractClaim> optional = ClaimList.getClaimAt(player);
        if (optional.isPresent() && optional.get().checkAction(player.getUUID(), Flags.CLAIM_FLY)) {
            player.gameMode.getGameModeForPlayer().updatePlayerAbilities(player.getAbilities());
            if (newValue) {
                player.getAbilities().mayfly = true;
            }
            player.onUpdateAbilities();
        }
        super.afterToggle(src, newValue);
    }
}
