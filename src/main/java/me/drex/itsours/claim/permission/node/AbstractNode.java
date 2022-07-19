package me.drex.itsours.claim.permission.node;

import me.drex.itsours.ItsOurs;
import net.minecraft.item.ItemConvertible;
import net.minecraft.text.MutableText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class AbstractNode implements Node {

    private final String id;
    private final MutableText description;
    private final List<Node> nodes;
    private final Map<String, Node> id2NodeMap;
    private final ItemConvertible icon;
    private final Predicate<ChangeContext> changePredicate;

    protected AbstractNode(String id, MutableText description, List<Node> nodes, ItemConvertible icon, Predicate<ChangeContext> changePredicate) {
        this.id = id;
        this.description = description;
        this.nodes = nodes;
        this.id2NodeMap = new HashMap<>();
        for (Node node : nodes) {
            if (id2NodeMap.containsKey(node.getId())) ItsOurs.LOGGER.warn("Found duplicate node {} in {}", node.getId(), this.getId());
            else id2NodeMap.put(node.getId(), node);
        }
        this.icon = icon;
        this.changePredicate = changePredicate;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public List<Node> getNodes() {
        return this.nodes;
    }

    @Override
    public Map<String, Node> getNodesMap() {
        return id2NodeMap;
    }

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public boolean canChange(ChangeContext context) {
        return changePredicate.test(context);
    }

    @Override
    public ItemConvertible getIcon() {
        return this.icon;
    }

    @Override
    public MutableText getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        return String.format("Node[type=%s, id=%s]", this.getClass().getSimpleName(), this.id);
    }
}
