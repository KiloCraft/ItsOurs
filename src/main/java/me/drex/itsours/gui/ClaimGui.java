package me.drex.itsours.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.command.RemoveCommand;
import me.drex.itsours.command.RenameCommand;
import me.drex.itsours.gui.claims.ClaimListGui;
import me.drex.itsours.gui.permission.SettingsGui;
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

    public ClaimGui(GuiContext context, AbstractClaim claim) {
        super(context, ScreenHandlerType.GENERIC_9X1);
        this.claim = claim;
    }

    @Override
    public void build() {
        this.setTitle(localized("text.itsours.gui.claim.title", claim.placeholders(context.server())));
        this.setSlot(0, switchElement(Items.REDSTONE, "claim.settings", new SettingsGui(context, claim, Permission.permission())));
        this.setSlot(1, switchElement(Items.PLAYER_HEAD, "claim.playermanager", new PlayerManagerGui(context, claim)));
        this.setSlot(2, switchElement(Items.HOPPER, "claim.rolemanager", new RoleManagerGui(context, claim)));
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
