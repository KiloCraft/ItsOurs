package me.drex.itsours.gui.screen;


import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.rework.Value;
import me.drex.itsours.claim.permission.util.node.util.Node;
import me.drex.itsours.command.SettingCommand;
import me.drex.itsours.gui.util.ScreenHelper;
import me.drex.itsours.gui.util.SlotEntry;
import me.drex.itsours.gui.util.context.ClaimContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

import java.util.Optional;

public class SettingScreen extends AbstractMapScreen<ClaimContext> {

    public SettingScreen(ServerPlayerEntity player, int rows, ClaimContext context, SimpleScreen<?> previous, Node node, Node.CompareMode compareMode, FilterMode filterMode) {
        super(player, rows, context, previous, node, compareMode, filterMode);
        ItemStack info = new ItemStack(Items.PAPER);
        ScreenHelper.setCustomName(info, Text.translatable("text.itsours.gui.map.settings.info"));
        ScreenHelper.addLore(info, Text.translatable("text.itsours.gui.map.settings.info.hover"));
        addSlot(new SlotEntry<>(info), 4);
    }

    @Override
    public void executeSet(String permission, Value value) {
        Optional<Permission> optional = Permission.setting(permission);
        if (optional.isPresent()) {
            try {
                SettingCommand.setSetting(player.getCommandSource(), context.getClaim(), optional.get(), value);
            } catch (CommandSyntaxException e) {
                player.getCommandSource().sendError(Texts.toText(e.getRawMessage()));
            }
        } else {
            ItsOurs.LOGGER.warn("Tried to set invalid permission: " + permission);
        }
    }

    @Override
    public Value getValue(String perm) {
        //return context.getClaim().getPermissionManager().settings.getValue(perm);
        // TODO:
        return Value.UNSET;
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
