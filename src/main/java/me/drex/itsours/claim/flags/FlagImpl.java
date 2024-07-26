package me.drex.itsours.claim.flags;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.claim.flags.node.RootNode;
import me.drex.itsours.claim.flags.util.InvalidFlagException;
import me.drex.itsours.command.argument.FlagArgument;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FlagImpl implements Flag {

    private final String literal;
    private final RootNode rootNode;
    private final ChildNode[] childNodes;

    protected FlagImpl(RootNode rootNode, String literal) throws InvalidFlagException {
        this(rootNode, parse(literal, rootNode));
    }

    protected FlagImpl(RootNode rootNode, ChildNode[] childNodes) {
        this.rootNode = rootNode;
        this.childNodes = childNodes;
        this.literal = String.join(".", Arrays.stream(childNodes).map(ChildNode::getId).toList());
    }

    private static ChildNode[] parse(String input, RootNode rootNode) throws InvalidFlagException {
        final List<ChildNode> nodes = new LinkedList<>();
        String[] parts = input.split("\\.", -1);
        Node currentNode = rootNode;
        for (String part : parts) {
            ChildNode node = currentNode.getNodesMap().get(part);
            if (node == null)
                throw new InvalidFlagException("Couldn't find " + part + " node in " + currentNode.getName());
            nodes.add(node);
            currentNode = node;
        }
        return nodes.toArray(new ChildNode[]{});
    }

    @Override
    public boolean includes(Flag other) {
        ChildNode[] otherNodes = other.getChildNodes();
        for (int i = 0; i < this.childNodes.length; i++) {
            ChildNode node = this.childNodes[i];
            if (otherNodes.length > i) {
                ChildNode otherNode = otherNodes[i];
                if (!node.contains(otherNode)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public Flag withNode(ChildNode childNode) throws InvalidFlagException {
        Node lastNode = getLastNode();
        if (!lastNode.getNodes().contains(childNode)) {
            throw new InvalidFlagException("Couldn't find " + childNode.getName() + " node in " + lastNode.getName());
        }
        ChildNode[] childNodes = Arrays.copyOf(this.childNodes, this.childNodes.length + 1);
        childNodes[this.childNodes.length] = childNode;
        return new FlagImpl(rootNode, childNodes);
    }

    @Override
    public void validateContext(Node.ChangeContext context) throws CommandSyntaxException {
        if (!canChange(context)) throw FlagArgument.FORBIDDEN;
    }

    @Override
    public boolean canChange(Node.ChangeContext context) {
        if (!rootNode.canChange(context)) return false;
        for (Node node : childNodes) {
            if (!node.canChange(context)) return false;
        }
        return true;
    }

    @Override
    public ChildNode[] getChildNodes() {
        return childNodes;
    }

    @Override
    public Node getLastNode() {
        if (childNodes.length != 0) {
            return childNodes[childNodes.length - 1];
        }
        return rootNode;
    }

    @Override
    public ChildNode getLastChildNode() {
        if (childNodes.length != 0) {
            return childNodes[childNodes.length - 1];
        }
        throw new IllegalStateException("This node doesn't have any child nodes");
    }

    @Override
    public String asString() {
        return this.literal;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FlagImpl flag) {
            return this.literal.equals(flag.literal);
        }
        return false;
    }

    @Override
    public String toString() {
        return literal;
    }

    @Override
    public int hashCode() {
        return literal.hashCode();
    }
}
