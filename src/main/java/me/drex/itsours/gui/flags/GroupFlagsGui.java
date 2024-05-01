package me.drex.itsours.gui.flags;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.context.GroupContext;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.claim.flags.util.Value;
import me.drex.itsours.claim.groups.Group;
import me.drex.itsours.command.GroupsCommand;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.function.Predicate;

import static me.drex.message.api.LocalizedMessage.localized;

public class GroupFlagsGui extends AbstractFlagsGui {

    private final AbstractClaim claim;
    private final String groupId;
    private final Group group;
    private final GroupContext groupContext;

    public GroupFlagsGui(GuiContext context, AbstractClaim claim, String groupId, Group group, Flag flag) {
        super(context, group.flags(), flag);
        this.claim = claim;
        this.groupId = groupId;
        this.group = group;
        this.groupContext = claim.getGroupManager().context(groupId, group);
        this.setTitle(localized("text.itsours.gui.group.title", PlaceholderUtil.mergePlaceholderMaps(
            Map.of("group_id", Text.literal(groupId)),
            Map.of("flag", Text.literal(flag.asString()))
        )));
    }

    @Override
    public Predicate<ChildNode> elementFilter() {
        return childNode -> childNode.canChange(new Node.ChangeContext(claim, groupContext, Value.DEFAULT, context.player.getCommandSource()));
    }

    @Override
    boolean setValue(Flag flag, Value value) {
        try {
            GroupsCommand.INSTANCE.setGroupFlags(context.player.getCommandSource().withSilent(), claim, groupId, flag, value);
            return true;
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    @Override
    AbstractFlagsGui create(Flag flag) {
        return new GroupFlagsGui(context, claim, groupId, group, flag);
    }
}
