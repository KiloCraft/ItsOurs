package me.drex.itsours.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.roles.ClaimRoleManager;
import me.drex.itsours.claim.roles.Role;
import me.drex.itsours.command.RolesCommand;
import me.drex.itsours.gui.permission.RoleStorageGui;
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

public class RoleManagerGui extends PageGui<String> {

    private final AbstractClaim claim;

    public RoleManagerGui(GuiContext context, AbstractClaim claim) {
        super(context, ScreenHandlerType.GENERIC_9X6);
        this.claim = claim;
        this.setTitle(localized("text.itsours.gui.rolemanager.title", claim.placeholders(context.server())));
    }

    @Override
    public Collection<String> elements() {
        return claim.getRoleManager().getRoleIds();
    }

    @Override
    protected GuiElementBuilder guiElement(String roleId) {
        Role role = claim.getRoleManager().roles().getOrDefault(roleId, new Role());
        return guiElement(ClaimRoleManager.getRoleIcon(roleId), "rolemanager.entry", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(context.server()),
            Map.of(
                "role_id", Text.literal(roleId)
            )
        ))
            .setCallback(clickType -> {
                if (clickType.isLeft) {
                    // noop
                } else if (clickType.isRight) {
                    switchUi(new RoleStorageGui(context, claim, roleId, role, Permission.permission(PermissionManager.PERMISSION)));
                } else if (clickType.isMiddle) {
                    switchUi(new ConfirmationGui(context, "text.itsours.gui.playermanager.remove.confirm", Map.of("role_id", Text.literal(roleId)), () -> removeRole(roleId)));
                }
            });
    }

    @Override
    public GuiElementBuilder buildNavigationBar(int index) {
        if (index == 0) {
            return new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(localized("text.itsours.gui.rolemanager.add"))
                .hideDefaultTooltip()
                .setSkullOwner(GuiTextures.GUI_ADD)
                .setCallback(() -> {
                    switchUi(new ValidStringInputGui(context, "", input -> claim.getRoleManager().getRole(input) == null, this::addRole) {
                    });
                });
        }
        return super.buildNavigationBar(index);
    }

    private void removeRole(String roleId) {
        try {
            if (RolesCommand.INSTANCE.deleteRole(context.player.getCommandSource(), claim, roleId) > 0) {
                click();
                build();
            } else {
                fail();
            }
        } catch (CommandSyntaxException e) {
            fail();
        }
    }

    private void addRole(String roleId) {
        try {
            RolesCommand.INSTANCE.createRole(context.player.getCommandSource(), claim, roleId);
            click();
            build();
        } catch (CommandSyntaxException e) {
            fail();
        }
        backCallback();
    }

}
