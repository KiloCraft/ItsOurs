package me.drex.itsours.claim.permission.util.node;

public class SingleNode extends AbstractNode {

    private final String id;

    public SingleNode(String id) {
        this.id = id;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public boolean contains(String node) {
        return node.equals(id);
    }
}
