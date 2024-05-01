package me.drex.itsours.claim.flags.util;

import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;

public enum Modify {

    SIZE("size", Items.PISTON),
    FLAG("flag", Items.REPEATER),
    MESSAGE("message", Items.PAPER),
    SUBZONE("subzone", Items.SPRUCE_DOOR),
    CHECK("check", Items.WRITABLE_BOOK);

    private final ChildNode node;

    Modify(String id, ItemConvertible icon) {
        this.node = Node.literal(id).icon(icon).description("flags.modify." + id).build();
    }

    public ChildNode node() {
        return node;
    }

}
