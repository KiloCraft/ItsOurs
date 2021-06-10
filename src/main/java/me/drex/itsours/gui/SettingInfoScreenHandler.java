package me.drex.itsours.gui;

import me.drex.itsours.claim.permission.util.PermissionMap;
import me.drex.itsours.claim.permission.util.node.util.Node;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class SettingInfoScreenHandler extends PermissionMapScreenHandler {

    protected SettingInfoScreenHandler(int syncId, PlayerEntity player, PermissionMap map, GUIScreenHandler previous, int page, Node... node) {
        super(syncId, player, map, previous, page, node);
    }

    public static void openMenu(ServerPlayerEntity player, PermissionMap map, GUIScreenHandler previous, int page, Node... node) {
        NamedScreenHandlerFactory factory = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new SettingInfoScreenHandler(syncId, player, map, previous, page, node);
            }

            @Override
            public Text getDisplayName() {
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < node.length; i++) {
                    s.append(node[i].getName());
                    if (i != node.length - 1) s.append(", ");
                }
                return new LiteralText("Settings (" + s + ")");
            }
        };
        player.openHandledScreen(factory);
    }

    public void open(ServerPlayerEntity player, PermissionMap map, GUIScreenHandler previous, int page, Node... node) {
        player.getServer().execute(() -> openMenu(player, map, previous, page, node));
    }

}
