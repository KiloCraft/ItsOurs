package me.drex.itsours.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.command.RemoveCommand;
import me.drex.itsours.command.RenameCommand;
import me.drex.itsours.gui.claims.ClaimListGui;
import me.drex.itsours.gui.flags.FlagsGui;
import me.drex.itsours.gui.flags.SimpleFlagsGui;
import me.drex.itsours.gui.players.AdvancedPlayerManagerGui;
import me.drex.itsours.gui.players.GroupManagerGui;
import me.drex.itsours.gui.players.SimplePlayerManagerGui;
import me.drex.itsours.gui.util.BaseGui;
import me.drex.itsours.gui.util.ConfirmationGui;
import me.drex.itsours.gui.util.ValidStringInputGui;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;

import java.util.Map;

import static me.drex.message.api.LocalizedMessage.localized;

public class ClaimGui extends BaseGui {

    private final AbstractClaim claim;
    private final boolean advanced;

    public ClaimGui(GuiContext context, AbstractClaim claim, boolean advanced) {
        super(context, ScreenHandlerType.GENERIC_9X1);
        this.claim = claim;
        this.advanced = advanced;
    }

    @Override
    public void build() {
        this.setTitle(localized("text.itsours.gui.claim.title", claim.placeholders(context.server())));
        if (advanced) {
            this.setSlot(0, switchElement(Items.REDSTONE, "claim.flags", new FlagsGui(context, claim, Flag.flag())));
        } else {
            this.setSlot(0, switchElement(Items.REDSTONE, "claim.flags", new SimpleFlagsGui(context, claim)));
        }
        if (advanced) {
            this.setSlot(1, switchElement(Items.PLAYER_HEAD, "claim.playermanager.advanced", new AdvancedPlayerManagerGui(context, claim)));
        } else {
            this.setSlot(1, switchElement(Items.PLAYER_HEAD, "claim.playermanager.simple", new SimplePlayerManagerGui(context, claim)));
        }
        if (advanced) {
            this.setSlot(2, switchElement(Items.HOPPER, "claim.groupmanager", new GroupManagerGui(context, claim)));
        }
        this.setSlot(6, switchElement(Items.NAME_TAG, "claim.rename", new ValidStringInputGui(context, claim.getName(), claim::canRename, input -> {
            switchUi(new ConfirmationGui(context, "text.itsours.gui.claim.rename.confirm", PlaceholderUtil.mergePlaceholderMaps(
                claim.placeholders(context.server()),
                Map.of("input", Text.literal(input))
            ), () -> rename(input)));
        })));
        this.setSlot(7, switchElement(Items.REDSTONE_BLOCK, "claim.remove", new ConfirmationGui(context, "text.itsours.gui.claim.remove.confirm", claim.placeholders(context.server()), () -> {
            try {
                while (!context.guiStack.isEmpty() && !(context.guiStack.peek() instanceof ClaimListGui<?>)) {
                    context.guiStack.pop();
                }
                RemoveCommand.INSTANCE.executeRemoveConfirmed(context.player.getCommandSource(), claim);
            } catch (CommandSyntaxException e) {
                fail();
            }
        })));
    }

    private void rename(String name) {
        try {
            // Go back to the previous gui
            context.guiStack.pop();
            RenameCommand.INSTANCE.executeRename(context.player.getCommandSource(), claim, name);
        } catch (CommandSyntaxException ignored) {
            fail();
        }
    }

}
