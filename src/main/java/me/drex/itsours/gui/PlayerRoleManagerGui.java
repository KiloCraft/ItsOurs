package me.drex.itsours.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.roles.ClaimRoleManager;
import me.drex.itsours.claim.roles.Role;
import me.drex.itsours.command.RolesCommand;
import me.drex.itsours.gui.permission.RoleStorageGui;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static me.drex.message.api.LocalizedMessage.localized;

public class PlayerRoleManagerGui extends PageGui<String> {

    private final AbstractClaim claim;
    private final UUID player;

    public PlayerRoleManagerGui(GuiContext context, AbstractClaim claim, UUID player) {
        super(context, ScreenHandlerType.GENERIC_9X6);
        this.claim = claim;
        this.player = player;
        this.setTitle(localized("text.itsours.gui.playerrolemanager.title", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(context.server()),
            PlaceholderUtil.uuid("player_", player, context.server())
        )));
    }

    @Override
    public Collection<String> elements() {
        return claim.getRoleManager().getRoleIds();
    }

    @Override
    protected GuiElementBuilder guiElement(String roleId) {
        Role role = claim.getRoleManager().roles().getOrDefault(roleId, new Role());
        boolean hasRole = role.players().contains(player);
        GuiElementBuilder builder = guiElement(ClaimRoleManager.getRoleIcon(roleId), "playerrolemanager.entry", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(context.server()),
            Map.of(
                "role_id", Text.literal(roleId).formatted(hasRole ? Formatting.GREEN : Formatting.RED)
            ),
            PlaceholderUtil.uuid("player_", player, context.server())
        )).setCallback(clickType -> {
            if (clickType.isLeft) {
                // Join / leave role
                boolean success = false;
                if (hasRole) {
                    try {
                        RolesCommand.INSTANCE.leaveRole(context.player.getCommandSource().withSilent(), claim, roleId, Collections.singleton(new GameProfile(player, player.toString())));
                        success = true;
                    } catch (CommandSyntaxException ignored) {
                    }
                } else {
                    try {
                        RolesCommand.INSTANCE.joinRole(context.player.getCommandSource().withSilent(), claim, roleId, Collections.singleton(new GameProfile(player, player.toString())));
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
                switchUi(new RoleStorageGui(context, claim, roleId, role, Permission.permission(PermissionManager.PERMISSION)));

            }
        });
        if (hasRole) {
            builder.glow();
        }
        return builder;
    }

}
