package me.drex.itsours.claim.permission.node;

import net.minecraft.item.ItemConvertible;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Predicate;

public class RootNode extends SingleNode {

    private final String name;

    public RootNode(String id, MutableText description, List<Node> nodes, ItemConvertible icon, Predicate<ChangeContext> changePredicate, String name) {
        super(id, description, nodes, icon, changePredicate);
        this.name = name;
    }

    public void addNode(Node node) {
        this.getNodes().add(node);
        this.getNodesMap().put(node.getId(), node);
    }

    @Override
    public String getName() {
        return "#" + name.toUpperCase();
    }
}
