package me.drex.itsours.gui.screen;


import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.util.node.util.Node;
import me.drex.itsours.command.SettingCommand;
import me.drex.itsours.gui.util.ScreenHelper;
import me.drex.itsours.gui.util.SlotEntry;
import me.drex.itsours.gui.util.context.ClaimContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Texts;

import java.util.Optional;

public class SettingScreen extends AbstractMapScreen<ClaimContext> {

    public SettingScreen(ServerPlayerEntity player, int rows, ClaimContext context, SimpleScreen<?> previous, Node node, Node.CompareMode compareMode, FilterMode filterMode) {
        super(player, rows, context, previous, node, compareMode, filterMode);
        ItemStack info = new ItemStack(Items.PAPER);
        ScreenHelper.setCustomName(info, "Settings");
        ScreenHelper.addLore(info, Component.text("This page allows").color(NamedTextColor.WHITE));
        ScreenHelper.addLore(info, Component.text("you to change the").color(NamedTextColor.WHITE));
        ScreenHelper.addLore(info, Component.text("default permissions").color(NamedTextColor.WHITE));
        ScreenHelper.addLore(info, Component.text("of your claim").color(NamedTextColor.WHITE));
        addSlot(new SlotEntry<>(info), 4);
    }

    @Override
    public void executeSet(String permission, Permission.Value value) {
        Optional<Permission> optional = Permission.setting(permission);
        if (optional.isPresent()) {
            try {
                SettingCommand.setSetting(player.getCommandSource(), context.getClaim(), optional.get(), value);
            } catch (CommandSyntaxException e) {
                player.getCommandSource().sendError(Texts.toText(e.getRawMessage()));
            }
        } else {
            ItsOursMod.LOGGER.warn("Tried to set invalid permission: " + permission);
        }
    }

    @Override
    public Permission.Value getValue(String perm) {
        return context.getClaim().getPermissionManager().settings.getValue(perm);
    }

    @Override
    public AbstractMapScreen<ClaimContext> buildScreen(ServerPlayerEntity player, int rows, ClaimContext context, SimpleScreen<?> previous, Node node, Node.CompareMode compareMode, FilterMode filterMode) {
        return new SettingScreen(player, rows, context, previous, node, compareMode, filterMode);
    }

    @Override
    protected String getTitle() {
        return "Settings (" + context.getClaim().getName() + ")";
    }

}
