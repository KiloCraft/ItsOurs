package me.drex.itsours.claim.permission.util;

import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;

import java.util.function.Predicate;

public enum Misc {

    ELYTRA("elytra", Items.ELYTRA, changeContext -> ItsOurs.hasPermission(changeContext.source(), "itsours.elytra"));

    private final Node node;

    Misc(String id, ItemConvertible icon) {
        this(id, icon, changeContext -> true);
    }

    Misc(String id, ItemConvertible icon, Predicate<Node.ChangeContext> predicate) {
        this.node = Node.single(id).icon(icon).description("permission.misc." + id).predicate(predicate).build();
    }

    public Node node() {
        return node;
    }

}
