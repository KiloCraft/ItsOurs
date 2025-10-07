package me.drex.itsours.gui.flags;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.context.PlayerContext;
import me.drex.itsours.claim.flags.holder.FlagData;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.claim.flags.util.Value;
import me.drex.itsours.command.PlayerFlagsCommand;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static me.drex.message.api.LocalizedMessage.localized;

public class PlayerFlagsGui extends AbstractFlagsGui {

    private final AbstractClaim claim;
    private final UUID player;

    public PlayerFlagsGui(GuiContext context, AbstractClaim claim, UUID player, Flag flag) {
        super(context, claim.getPlayerFlags().getOrDefault(player, new FlagData()), flag);
        this.claim = claim;
        this.player = player;
        this.setTitle(localized("text.itsours.gui.playerflags.title", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(context.server()),
            PlaceholderUtil.uuid("player_", player, context.server()),
            Map.of("flag", Text.literal(flag.asString()))
        )));
    }

    @Override
    public Predicate<ChildNode> elementFilter() {
        return childNode -> childNode.canChange(new Node.ChangeContext(claim, PlayerContext.INSTANCE, Value.DEFAULT, context.player.getCommandSource()));
    }

    @Override
    boolean setValue(Flag flag, Value value) {
        try {
            PlayerFlagsCommand.INSTANCE.executeSet(context.player.getCommandSource().withSilent(), claim, Collections.singleton(new PlayerConfigEntry(player, player.toString())), flag, value);
            return true;
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    @Override
    AbstractFlagsGui create(Flag flag) {
        return new PlayerFlagsGui(context, claim, player, flag);
    }
}
