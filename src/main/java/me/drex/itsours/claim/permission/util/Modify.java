package me.drex.itsours.claim.permission.util;

import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;

public enum Modify {

    TRUST("trust", Items.EMERALD),
    DISTRUST("distrust", Items.REDSTONE),
    SIZE("size", Items.PISTON),
    PERMISSION("permission", Items.REPEATER),
    MESSAGE("message", Items.PAPER),
    SETTING("setting", Items.COMPARATOR),
    SUBZONE("subzone", Items.SPRUCE_DOOR),
    CHECK("check", Items.WRITABLE_BOOK),
    ROLE("role", Items.LEATHER_CHESTPLATE);

    private final String id;
    private final ItemConvertible icon;
    private final Node node;

    Modify(String id, ItemConvertible icon) {
        this.id = id;
        this.icon = icon;
        this.node = Node.single(id).icon(icon).build();
    }

    public Node buildNode() {
        return node;
    }

}
