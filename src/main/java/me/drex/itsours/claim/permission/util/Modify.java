package me.drex.itsours.claim.permission.util;

import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;

public enum Modify {

    TRUST("trust", Items.EMERALD),
    UNTRUST("untrust", Items.GRAY_DYE),
    DISTRUST("distrust", Items.REDSTONE),
    SIZE("size", Items.PISTON),
    PERMISSION("permission", Items.REPEATER),
    SETTING("setting", Items.COMPARATOR),
    SUBZONE("subzone", Items.SPRUCE_DOOR),
    NAME("name", Items.NAME_TAG),
    ROLE("role", Items.LEATHER_CHESTPLATE);

    private final String id;
    private final ItemConvertible icon;

    Modify(String id, ItemConvertible icon) {
        this.id = id;
        this.icon = icon;
    }

    public Node buildNode() {
        return Node.single(id).icon(icon).build();
    }

}
