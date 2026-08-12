package me.drex.itsours.claim.flags.util;

import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.Items;

public enum Modify {

    SIZE("size", Items.PISTON),
    FLAG("flag", Items.REPEATER),
    MESSAGE("message", Items.PAPER),
    SUBZONE("subzone", Items.SPRUCE_DOOR),
    CHECK("check", Items.WRITABLE_BOOK);

    private final ChildNode node;

    Modify(String id, ItemLike icon) {
        this.node = Node.literal(id).icon(icon).description("modify." + id).build();
    }

    public ChildNode node() {
        return node;
    }

}
