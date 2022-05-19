package me.drex.itsours.gui.screen;

import me.drex.itsours.claim.permission.rework.Value;
import me.drex.itsours.claim.permission.util.node.util.Node;
import me.drex.itsours.gui.util.ScreenHelper;
import me.drex.itsours.gui.util.SlotEntry;
import me.drex.itsours.gui.util.context.PermissionContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class PermissionScreen extends AbstractMapScreen<PermissionContext> {

    public PermissionScreen(ServerPlayerEntity player, int rows, PermissionContext context, SimpleScreen<?> previous, Node node, Node.CompareMode compareMode, FilterMode filterMode) {
        super(player, rows, context, previous, node, compareMode, filterMode);
        ItemStack info = ScreenHelper.createPlayerHead(context.getUUID());
        ScreenHelper.setCustomName(info, Text.translatable("text.itsours.gui.map.permission.info"));
        ScreenHelper.addLore(info, Text.translatable("text.itsours.gui.map.permission.info.hover", ScreenHelper.toName(context.getUUID())));
        addSlot(new SlotEntry<>(info), 4);
    }

    @Override
    protected String getTitle() {
        return "Permission Manager (" + context.getClaim().getName() + ")";
    }

    @Override
    public void executeSet(String id, Value value) {
        /*GameProfile target = ScreenHelper.getProfile(context.getUUID());
        try {
            PermissionRework permission = PermissionRework.permission(id);
            //PermissionCommand.setPermission(player.getCommandSource(), context.getClaim(), target, permission, value);
        } catch (InvalidPermissionException e) {
            ItsOurs.LOGGER.warn(e);
        }
        Optional<Permission> optional = Permission.permission(id);
        if (optional.isPresent()) {
            try {
                //PermissionCommand.setPermission(player.getCommandSource(), context.getClaim(), target, optional.get(), value);
            } catch (CommandSyntaxException e) {
                player.getCommandSource().sendError(Texts.toText(e.getRawMessage()));
            }
        } else {
            ItsOurs.LOGGER.warn("Tried to set invalid permission: " + id);
        }*/
    }

    @Override
    public Value getValue(String perm) {
        // TODO:
        return Value.UNSET;
        //return context.getClaim().getPermissionManager().getPlayerPermission(context.getUUID()).getValue(perm);
    }

    @Override
    public AbstractMapScreen<PermissionContext> buildScreen(ServerPlayerEntity player, int rows, PermissionContext context, SimpleScreen<?> previous, Node node, Node.CompareMode compareMode, FilterMode filterMode) {
        return new PermissionScreen(player, rows, context, previous, node, compareMode, filterMode);
    }
}
