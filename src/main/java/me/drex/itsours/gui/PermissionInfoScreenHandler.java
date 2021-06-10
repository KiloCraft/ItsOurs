package me.drex.itsours.gui;

import me.drex.itsours.claim.permission.util.PermissionMap;
import me.drex.itsours.claim.permission.util.node.util.Node;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.UUID;

public class PermissionInfoScreenHandler extends PermissionMapScreenHandler {

    protected final UUID uuid;

    protected PermissionInfoScreenHandler(int syncId, PlayerEntity player, PermissionMap map, GUIScreenHandler previous, int page, UUID uuid, Node... node) {
        super(syncId, player, map, previous, page, node);
        this.uuid = uuid;
    }

    public static void openMenu(ServerPlayerEntity player, PermissionMap map, GUIScreenHandler previous, int page, UUID uuid, Node... node) {
        NamedScreenHandlerFactory factory = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new PermissionInfoScreenHandler(syncId, player, map, previous, page, uuid, node);
            }

            @Override
            public Text getDisplayName() {
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < node.length; i++) {
                    s.append(node[i].getName());
                    if (i != node.length - 1) s.append(", ");
                }
                return new LiteralText("Permissions (" + s + ")");
            }
        };
        player.openHandledScreen(factory);
    }

    @Override
    protected void fillInventory(PlayerEntity player, Inventory inv) {
        inventory.setStack(4, ScreenHelper.createPlayerHead(uuid));
        super.fillInventory(player, inv);
        inventory.setStack(4, ScreenHelper.createPlayerHead(uuid));
    }

    public void open(ServerPlayerEntity player, PermissionMap map, GUIScreenHandler previous, int page, Node... node) {
        if (previous instanceof PermissionInfoScreenHandler) player.getServer().execute(() -> openMenu(player, map, previous, page, ((PermissionInfoScreenHandler) previous).uuid, node));
    }

}
