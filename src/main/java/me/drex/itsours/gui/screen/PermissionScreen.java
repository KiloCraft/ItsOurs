package me.drex.itsours.gui.screen;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.util.node.util.Node;
import me.drex.itsours.command.PermissionCommand;
import me.drex.itsours.gui.util.ScreenHelper;
import me.drex.itsours.gui.util.SlotEntry;
import me.drex.itsours.gui.util.context.PermissionContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Texts;

import java.util.Optional;

public class PermissionScreen extends AbstractMapScreen<PermissionContext> {

    public PermissionScreen(ServerPlayerEntity player, int rows, PermissionContext context, SimpleScreen<?> previous, Node node) {
        super(player, rows, context, previous, node);
        ItemStack info = ScreenHelper.createPlayerHead(context.getUUID());
        ScreenHelper.setCustomName(info, "Permissions");
        ScreenHelper.addLore(info, Component.text("This page allows").color(NamedTextColor.WHITE));
        ScreenHelper.addLore(info, Component.text("you to change").color(NamedTextColor.WHITE));
        ScreenHelper.addLore(info, Component.text(ScreenHelper.toName(context.getUUID()) + "'s permissions").color(NamedTextColor.WHITE));
        addSlot(new SlotEntry<>(info), 4);
    }

    @Override
    protected String getTitle() {
        return "Permission Manager (" + context.getClaim().getName() + ")";
    }

    @Override
    public void executeSet(String permission, Permission.Value value) {
        GameProfile target = ScreenHelper.getProfile(context.getUUID());
        Optional<Permission> optional = Permission.permission(permission);
        if (optional.isPresent()) {
            try {
                PermissionCommand.setPermission(player.getCommandSource(), context.getClaim(), target, optional.get(), value);
            } catch (CommandSyntaxException e) {
                player.getCommandSource().sendError(Texts.toText(e.getRawMessage()));
            }
        } else {
            ItsOursMod.LOGGER.warn("Tried to set invalid permission: " + permission);
        }
    }

    @Override
    public Permission.Value getValue(String perm) {
        return context.getClaim().getPermissionManager().getPlayerPermission(context.getUUID()).getValue(perm);
    }

    @Override
    public AbstractMapScreen<PermissionContext> buildScreen(ServerPlayerEntity player, int rows, PermissionContext context, SimpleScreen<?> previous, Node node) {
        return new PermissionScreen(player, rows, context, this, node);
    }

}
