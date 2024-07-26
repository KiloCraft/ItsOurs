package me.drex.itsours.gui.players;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.groups.ClaimGroupManager;
import me.drex.itsours.claim.groups.Group;
import me.drex.itsours.command.GroupsCommand;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.gui.PageGui;
import me.drex.itsours.gui.flags.GroupFlagsGui;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static me.drex.message.api.LocalizedMessage.localized;

public class PlayerGroupManagerGui extends PageGui<String> {

    private final AbstractClaim claim;
    private final UUID player;

    public PlayerGroupManagerGui(GuiContext context, AbstractClaim claim, UUID player) {
        super(context, ScreenHandlerType.GENERIC_9X6);
        this.claim = claim;
        this.player = player;
        this.setTitle(localized("text.itsours.gui.playergroupmanager.title", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(context.server()),
            PlaceholderUtil.uuid("player_", player, context.server())
        )));
    }

    @Override
    public Collection<String> elements() {
        return claim.getGroupManager().getGroupIds();
    }

    @Override
    protected GuiElementBuilder guiElement(String groupId) {
        Group group = claim.getGroupManager().groups().getOrDefault(groupId, new Group());
        boolean hasGroup = group.players().contains(player);
        GuiElementBuilder builder = guiElement(ClaimGroupManager.getGroupIcon(groupId), "playergroupmanager.entry", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(context.server()),
            Map.of(
                "group_id", Text.literal(groupId).formatted(hasGroup ? Formatting.GREEN : Formatting.RED)
            ),
            PlaceholderUtil.uuid("player_", player, context.server())
        )).setCallback(clickType -> {
            if (clickType.isLeft) {
                // Join / leave group
                boolean success = false;
                if (hasGroup) {
                    try {
                        GroupsCommand.INSTANCE.leaveGroup(context.player.getCommandSource().withSilent(), claim, groupId, Collections.singleton(new GameProfile(player, player.toString())));
                        success = true;
                    } catch (CommandSyntaxException ignored) {
                    }
                } else {
                    try {
                        GroupsCommand.INSTANCE.joinGroup(context.player.getCommandSource().withSilent(), claim, groupId, Collections.singleton(new GameProfile(player, player.toString())));
                        success = true;
                    } catch (CommandSyntaxException ignored) {
                    }
                }
                if (success) {
                    click();
                    build();
                } else {
                    fail();
                }
            } else if (clickType.isRight) {
                switchUi(new GroupFlagsGui(context, claim, groupId, group, Flag.flag(Flags.PLAYER)));

            }
        });
        if (hasGroup) {
            builder.glow();
        }
        return builder;
    }

}
