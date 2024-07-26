package me.drex.itsours.claim.flags;

import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.Node;

public class UnknownFlag implements Flag {

    protected UnknownFlag() {
    }

    @Override
    public boolean includes(Flag other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validateContext(Node.ChangeContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canChange(Node.ChangeContext context) {
        return false;
    }

    @Override
    public Flag withNode(ChildNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChildNode[] getChildNodes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node getLastNode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ChildNode getLastChildNode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String asString() {
        throw new UnsupportedOperationException();
    }
}
