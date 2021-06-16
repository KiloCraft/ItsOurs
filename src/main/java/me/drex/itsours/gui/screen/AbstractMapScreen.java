package me.drex.itsours.gui.screen;

import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.util.node.util.GroupNode;
import me.drex.itsours.claim.permission.util.node.util.Node;
import me.drex.itsours.gui.util.ScreenHelper;
import me.drex.itsours.gui.util.SlotEntry;
import me.drex.itsours.gui.util.context.ClaimContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public abstract class AbstractMapScreen<K extends ClaimContext> extends PagedScreen<K> {

    protected final Node node;
    protected List<Node> nodes;
    private Node.CompareMode compareMode = Node.CompareMode.ALPHABET_DESC;

    public AbstractMapScreen(ServerPlayerEntity player, int rows, K context, SimpleScreen<?> previous, Node node) {
        super(player, rows, context, previous);
        this.node = node;
        if (node instanceof GroupNode) {
            nodes = node.getContained();
        } else {
            nodes = node.getNodes();
        }
        sort();
    }

    public void sort() {
        nodes.sort((o1, o2) -> o1.compareTo(o2, compareMode));
    }

    public String getPermission() {
        if (previous instanceof AbstractMapScreen permissionScreen) {
            return permissionScreen.getPermission() + node.getId();
        } else {
            return node.getId();
        }
    }

    public ItemStack buildItem(Node node) {
        ItemStack item = new ItemStack(node.getItem());
        if (item.isEmpty()) item = new ItemStack(Items.BARRIER);
        if (!(this.node instanceof GroupNode)) {
            String s = getPermission();
            String perm = s.equals("") ? node.getId() : s + "." + node.getId();
            Permission.Value value = getValue(perm);
            TextComponent val = Component.text("Value: ").color(NamedTextColor.WHITE);
            switch (value) {
                case TRUE -> {
                    ScreenHelper.addGlint(item);
                    ScreenHelper.addLore(item, val.append(Component.text("True").color(NamedTextColor.GREEN)));
                }
                case FALSE -> {
                    ScreenHelper.addLore(item, val.append(Component.text("False").color(NamedTextColor.RED)));
                }
                case UNSET -> {
                    ScreenHelper.addLore(item, val.append(Component.text("Unset").color(NamedTextColor.GRAY)));
                }
                default -> throw new IllegalStateException("Unexpected value: " + value);
            }
            ScreenHelper.setCustomName(item, perm);
            ScreenHelper.addLore(item, "Leftclick to cycle");
            if (!node.getNodes().isEmpty() || node instanceof GroupNode) ScreenHelper.addLore(item, "Rightclick for subnodes");
        } else {
            ScreenHelper.setCustomName(item, node.getId());
        }
        if (!node.getInformation().equals("-"))
            ScreenHelper.addLore(item, Component.text(node.getInformation()).color(NamedTextColor.AQUA));
        return item;
    }

    public abstract void executeSet(String permission, Permission.Value value);

    public abstract Permission.Value getValue(String perm);

    public abstract AbstractMapScreen<K> buildScreen(ServerPlayerEntity player, int rows, K context, SimpleScreen<?> previous, Node node);

        @Override
    public void draw() {
        ItemStack orderItem = new ItemStack(Items.COMPASS);
        ScreenHelper.setCustomName(orderItem, "Sort by");
        for (Node.CompareMode value : Node.CompareMode.values()) {
            ScreenHelper.addLore(orderItem, Component.text(value.getName()).color(value == compareMode ? NamedTextColor.AQUA : NamedTextColor.GRAY));
        }
        SlotEntry<K> order = new SlotEntry<>(orderItem, (permissionContext, leftClick, shiftClick) -> {
            int length = Node.CompareMode.values().length;
            compareMode = Node.CompareMode.values()[(compareMode.ordinal() + (leftClick ? 1 : length - 1)) % length];
            sort();
            draw();
        });
        addSlot(order, 8);

        for (Node n : nodes) {
            SlotEntry<K> slotEntry = new SlotEntry<>(buildItem(n), (claimContext, leftClick, shiftClick) -> {
                if (leftClick && !(this.node instanceof GroupNode)) {
                    //Switch state
                    String s = getPermission();
                    String perm = s.equals("") ? n.getId() : s + "." + n.getId();
                    Permission.Value value = getValue(perm);
                    int ordinal = value.ordinal() + 1;
                    Permission.Value next = Permission.Value.values()[ordinal % 3];
                    executeSet(perm, next);
                    draw();
                } else if (!n.getNodes().isEmpty() || n instanceof GroupNode) {
                    //Open subnodes
                    close();
                    AbstractMapScreen<K> screen = buildScreen(player, rows, context, this, n);
                    screen.render();
                }
            });
            addPageEntry(slotEntry);
        }

        super.draw();
    }

}
