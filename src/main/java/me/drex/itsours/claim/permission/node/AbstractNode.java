package me.drex.itsours.claim.permission.node;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static me.drex.itsours.ItsOurs.LOGGER;

public abstract class AbstractNode implements Node {

    protected final List<ChildNode> nodes;
    protected final Map<String, ChildNode> id2NodeMap;
    protected final Predicate<ChangeContext> changePredicate;

    protected AbstractNode(List<ChildNode> nodes, Predicate<ChangeContext> changePredicate) {
        this.nodes = nodes;
        this.id2NodeMap = new HashMap<>();
        Iterator<ChildNode> iterator = nodes.iterator();
        while (iterator.hasNext()) {
            ChildNode node = iterator.next();
            if (id2NodeMap.containsKey(node.getId())) {
                LOGGER.warn("Found duplicate node {} in {}", node.getId(), this.getName());
                iterator.remove();
            } else {
                id2NodeMap.put(node.getId(), node);
            }
        }
        this.changePredicate = changePredicate;
    }

    @Override
    public List<ChildNode> getNodes() {
        return this.nodes;
    }

    @Override
    public Map<String, ChildNode> getNodesMap() {
        return id2NodeMap;
    }

    @Override
    public boolean canChange(ChangeContext context) {
        return changePredicate.test(context);
    }

}
