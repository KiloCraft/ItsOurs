package me.drex.itsours.gui;

import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.util.PermissionMap;
import me.drex.itsours.claim.permission.util.node.PermissionNode;
import me.drex.itsours.claim.permission.util.node.util.GroupNode;
import me.drex.itsours.claim.permission.util.node.util.Node;
import me.drex.itsours.util.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public abstract class PermissionMapScreenHandler extends PagedScreenHandler {

    protected final PermissionMap map;
    protected final Node[] node;
    protected final List<Node> nodes;
    private int maxPage;

    protected PermissionMapScreenHandler(int syncId, PlayerEntity player, PermissionMap map, GUIScreenHandler previous, int page, Node... node) {
        super(syncId, 6, player);
        this.map = map;
        this.node = node;
        this.previous = previous;
        this.page = page;
        List<Node> nodes = new ArrayList<>();
        for (Node n : node) {
            if (n instanceof GroupNode) {
                nodes.addAll(n.getContained());
            }
            nodes.addAll(n.getNodes());
        }
        this.nodes = nodes;
        fillInventory(player, inventory);
    }

    /*public static void openMenu(ServerPlayerEntity player, PermissionMap map, GUIScreenHandler previous, int page, Node... node) {
        NamedScreenHandlerFactory factory = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new PermissionMapScreenHandler(syncId, player, map, previous, page, node);
            }

            @Override
            public Text getDisplayName() {
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < node.length; i++) {
                    s.append(node[i].getName());
                    if (i != node.length - 1) s.append(", ");
                }
                return new LiteralText("PermissionMap (" + s + ")");
            }
        };
        player.openHandledScreen(factory);
    }*/

    @Override
    public int getMaxPage() {
        return maxPage;
    }

    @Override
    protected void fillInventory(PlayerEntity player, Inventory inv) {
        inv.clear();
        int entriesPerPage = 28;
        int i = page * entriesPerPage;

        addBack();

        maxPage = (nodes.size() - 1) / entriesPerPage;
        super.fillInventory(player, inv);
        for (int slotIndex = 0; slotIndex < 45; slotIndex++) {
            if (slotIndex < 9 || slotIndex % 9 == 0 || slotIndex % 9 == 8) continue;
            if (i >= nodes.size()) break;
            Node n = nodes.get(i);
            ItemStack item = new ItemStack(n.getItem());
            if (item.isEmpty()) item = new ItemStack(Items.BARRIER);
            if (!(node[0] instanceof GroupNode)) {
                String s = getPermission();
                String perm = s.equals("") ? n.getId() : s + "." + n.getId();
                Permission.Value value = map.getValue(perm);
                Formatting formatting;
                switch (value) {
                    case TRUE -> {
                        ScreenHelper.addGlint(item);
                        ScreenHelper.addLore(item, TextComponentUtil.of("<green><underlined>True<reset> <white>/ <red>False<reset> <white>/ <yellow>Unset"));
                        formatting = Formatting.GREEN;
                    }
                    case FALSE -> {
                        ScreenHelper.addLore(item, TextComponentUtil.of("<green>True<reset> <white>/ <red><underlined>False<reset> <white>/ <yellow>Unset"));
                        formatting = Formatting.RED;
                    }
                    case UNSET -> {
                        ScreenHelper.addLore(item, TextComponentUtil.of("<green>True<reset> <white>/ <red>False<reset> <white>/ <yellow><underlined>Unset"));
                        formatting = Formatting.YELLOW;

                    }
                    default -> throw new IllegalStateException("Unexpected value: " + value);
                }
                item.setCustomName(new LiteralText(n.getId()).formatted(formatting));
                ScreenHelper.addLore(item, TextComponentUtil.of("<white>Leftclick to cycle"));
                if (!n.getNodes().isEmpty() || nodes.get(i) instanceof GroupNode) ScreenHelper.addLore(item, TextComponentUtil.of("<white>Rightclick for subnodes"));
            } else {
                item.setCustomName(new LiteralText(n.getId()).formatted(Formatting.GRAY));
            }
            if (!n.getInformation().equals("-")) ScreenHelper.addLore(item, TextComponentUtil.of("<aqua>" + n.getInformation()));
            inventory.setStack(slotIndex, item);
            i++;
        }
        fillEmpty();

    }

    public String getPermission() {
        if (previous instanceof PermissionMapScreenHandler) {
            return ((PermissionMapScreenHandler) previous).getPermission() + getPerm();
        } else {
            return getPerm();
        }
    }

    private String getPerm() {
        for (Node n : node) {
            if (n instanceof PermissionNode) {
                return n.getId();
            }
        }
        return node[0].getId();
    }

    protected abstract void open(ServerPlayerEntity player, PermissionMap map, GUIScreenHandler previous, int page, Node... node);

    @Override
    protected void handleSlotClick(ServerPlayerEntity player, int index, Slot slot, boolean leftClick, boolean shift) {
        if (index < 9 || index % 9 == 0 || index % 9 == 8) {
            switch (index) {
                case 0 -> {
                    ScreenHelper.openPrevious(player, this);
                }
            }
        } else {
            int line = index / 9;
            int i = index - (10 + ((line - 1) * 2)) + (page * 28);
            Node n = nodes.get(i);
            if (n != null && line != 5) {
                if (leftClick && !(node[0] instanceof GroupNode)) {
                    //Switch state
                    String s = getPermission();
                    String perm = s.equals("") ? n.getId() : s + "." + n.getId();
                    Permission.Value value = map.getValue(perm);
                    int ordinal = value.ordinal() + 1;
                    Permission.Value next = Permission.Value.values()[ordinal % 3];
                    //TODO: Check for permission
                    map.setPermission(perm, next);
                    fillInventory(player, inventory);
                } else if (!n.getNodes().isEmpty() || nodes.get(i) instanceof GroupNode) {
                    //Open subnodes
                    player.closeHandledScreen();
                    player.getServer().execute(() -> open(player, map, this, 0, n));
                }
            }
        }

        super.handleSlotClick(player, index, slot, leftClick, shift);
    }
}
