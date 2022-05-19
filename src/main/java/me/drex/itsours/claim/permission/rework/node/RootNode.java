package me.drex.itsours.claim.permission.rework.node;

import net.minecraft.item.ItemConvertible;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Predicate;

public class RootNode extends SingleNode {

    private final String name;

    public RootNode(String id, Text description, List<Node> nodes, ItemConvertible icon, Predicate<ChangeContext> changePredicate, String name) {
        super(id, description, nodes, icon, changePredicate);
        this.name = name;
    }

    // TODO: Remove and use builder properly instead
    public void addNode(Node node) {
        this.getNodes().add(node);
        this.getNodesMap().put(node.getId(), node);
    }

    @Override
    public String getName() {
        return "#" + name.toUpperCase();
    }
}
