package me.drex.itsours.gui.players;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.groups.ClaimGroupManager;
import me.drex.itsours.claim.groups.Group;
import me.drex.itsours.command.GroupsCommand;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.gui.PageGui;
import me.drex.itsours.gui.flags.GroupFlagsGui;
import me.drex.itsours.gui.util.ConfirmationGui;
import me.drex.itsours.gui.util.GuiTextures;
import me.drex.itsours.gui.util.ValidStringInputGui;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Map;

import static me.drex.message.api.LocalizedMessage.localized;

public class GroupManagerGui extends PageGui<String> {

    private final AbstractClaim claim;

    public GroupManagerGui(GuiContext context, AbstractClaim claim) {
        super(context, ScreenHandlerType.GENERIC_9X6);
        this.claim = claim;
        this.setTitle(localized("text.itsours.gui.groupmanager.title", claim.placeholders(context.server())));
    }

    @Override
    public Collection<String> elements() {
        return claim.getGroupManager().getGroupIds();
    }

    @Override
    protected GuiElementBuilder guiElement(String groupId) {
        Group group = claim.getGroupManager().groups().getOrDefault(groupId, new Group());
        return guiElement(ClaimGroupManager.getGroupIcon(groupId), "groupmanager.entry", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(context.server()),
            Map.of(
                "group_id", Text.literal(groupId)
            )
        ))
            .setCallback(clickType -> {
                if (clickType.isLeft) {
                    // noop
                } else if (clickType.isRight) {
                    switchUi(new GroupFlagsGui(context, claim, groupId, group, Flag.flag(Flags.PLAYER)));
                } else if (clickType.isMiddle) {
                    switchUi(new ConfirmationGui(context, "text.itsours.gui.playermanager.remove.confirm", Map.of("group_id", Text.literal(groupId)), () -> removeGroup(groupId)));
                }
            });
    }

    @Override
    public GuiElementBuilder buildNavigationBar(int index) {
        if (index == 0) {
            return new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(localized("text.itsours.gui.groupmanager.add"))
                .hideDefaultTooltip()
                .setSkullOwner(GuiTextures.GUI_ADD)
                .setCallback(() -> {
                    switchUi(new ValidStringInputGui(context, "", input -> claim.getGroupManager().getGroup(input) == null, this::addGroup) {
                    });
                });
        }
        return super.buildNavigationBar(index);
    }

    private void removeGroup(String groupId) {
        try {
            if (GroupsCommand.INSTANCE.deleteGroup(context.player.getCommandSource(), claim, groupId) > 0) {
                click();
                build();
            } else {
                fail();
            }
        } catch (CommandSyntaxException e) {
            fail();
        }
    }

    private void addGroup(String groupId) {
        try {
            GroupsCommand.INSTANCE.createGroup(context.player.getCommandSource(), claim, groupId);
            click();
            build();
        } catch (CommandSyntaxException e) {
            fail();
        }
        backCallback();
    }

}
