package me.drex.itsours.gui.flags;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.context.GlobalContext;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.claim.flags.util.Value;
import me.drex.itsours.command.FlagsCommand;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.function.Predicate;

import static me.drex.message.api.LocalizedMessage.localized;

public class FlagsGui extends AbstractFlagsGui {

    private final AbstractClaim claim;

    public FlagsGui(GuiContext context, AbstractClaim claim, Flag flag) {
        super(context, claim.getFlags(), flag);
        this.claim = claim;
        this.setTitle(localized("text.itsours.gui.flags.advanced.title", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(context.server()),
            Map.of("flag", Text.literal(flag.asString()))
        )));
    }

    @Override
    public Predicate<ChildNode> elementFilter() {
        return childNode -> childNode.canChange(new Node.ChangeContext(claim, GlobalContext.INSTANCE, Value.DEFAULT, context.player.getCommandSource()));
    }

    @Override
    boolean setValue(Flag flag, Value value) {
        try {
            FlagsCommand.INSTANCE.executeSet(player.getCommandSource().withSilent(), claim, flag, value);
            return true;
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    @Override
    AbstractFlagsGui create(Flag flag) {
        return new FlagsGui(context, claim, flag);
    }

}
