package me.drex.itsours.claim.permission;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.permission.node.ChildNode;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.claim.permission.node.RootNode;
import me.drex.itsours.claim.permission.util.InvalidPermissionException;
import me.drex.itsours.command.argument.PermissionArgument;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PermissionImpl implements Permission {

    private final String permission;
    private final RootNode rootNode;
    private final ChildNode[] childNodes;

    protected PermissionImpl(RootNode rootNode, String permission) throws InvalidPermissionException {
        this(rootNode, parse(permission, rootNode));
    }

    protected PermissionImpl(RootNode rootNode, ChildNode[] childNodes) {
        this.rootNode = rootNode;
        this.childNodes = childNodes;
        this.permission = String.join(".", Arrays.stream(childNodes).map(ChildNode::getId).toList());
    }

    private static ChildNode[] parse(String permission, RootNode rootNode) throws InvalidPermissionException {
        final List<ChildNode> nodes = new LinkedList<>();
        String[] parts = permission.split("\\.", -1);
        Node currentNode = rootNode;
        for (String part : parts) {
            ChildNode node = currentNode.getNodesMap().get(part);
            if (node == null)
                throw new InvalidPermissionException("Couldn't find " + part + " node in " + currentNode.getName());
            nodes.add(node);
            currentNode = node;
        }
        return nodes.toArray(new ChildNode[]{});
    }

    @Override
    public boolean includes(Permission other) {
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
    public Permission withNode(ChildNode childNode) throws InvalidPermissionException {
        Node lastNode = getLastNode();
        if (!lastNode.getNodes().contains(childNode)) {
            throw new InvalidPermissionException("Couldn't find " + childNode.getName() + " node in " + lastNode.getName());
        }
        ChildNode[] childNodes = Arrays.copyOf(this.childNodes, this.childNodes.length + 1);
        childNodes[this.childNodes.length] = childNode;
        return new PermissionImpl(rootNode, childNodes);
    }

    @Override
    public void validateContext(Node.ChangeContext context) throws CommandSyntaxException {
        if (!rootNode.canChange(context)) throw PermissionArgument.FORBIDDEN;
        for (Node node : childNodes) {
            if (!node.canChange(context)) throw PermissionArgument.FORBIDDEN;
        }
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
    public String asString() {
        return this.permission;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PermissionImpl permission) {
            return this.permission.equals(permission.permission);
        }
        return false;
    }

    @Override
    public String toString() {
        return permission;
    }

    @Override
    public int hashCode() {
        return permission.hashCode();
    }
}
