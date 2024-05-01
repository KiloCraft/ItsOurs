package me.drex.itsours.claim.flags.node;

import java.util.LinkedList;

import static me.drex.itsours.ItsOurs.LOGGER;

public class RootNode extends AbstractNode {

    private final String name;

    public RootNode(String name) {
        super(new LinkedList<>(), context -> true);
        this.name = name;
    }

    public void registerNode(ChildNode node) {
        if (id2NodeMap.containsKey(node.getId())) {
            LOGGER.warn("Found duplicate node {} in root {}", node.getId(), this.getName());
        } else {
            this.nodes.add(node);
            this.id2NodeMap.put(node.getId(), node);
        }
    }

    @Override
    public String getName() {
        return "#" + name.toUpperCase();
    }
}
