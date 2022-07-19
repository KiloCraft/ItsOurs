package me.drex.itsours.claim.permission;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.permission.context.GlobalContext;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.claim.permission.node.RootNode;
import me.drex.itsours.claim.permission.util.InvalidPermissionException;
import me.drex.itsours.command.argument.PermissionArgument;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PermissionImpl implements Permission {

    public static final String SEPARATOR = ".";
    public static final String SEPARATOR_REGEX = "\\.";

    private final String permission;
    private final List<Node> nodes;

    private PermissionImpl(String permission, RootNode rootNode) throws InvalidPermissionException {
        this.permission = permission;
        this.nodes = parse(rootNode);
    }

    private PermissionImpl(List<Node> nodes) {
        this.nodes = nodes;
        this.permission = String.join(SEPARATOR, nodes.stream().filter(node -> !(node instanceof RootNode)).map(Node::getId).toList());
    }

    public static PermissionImpl permission(String permission) throws InvalidPermissionException {
        return fromId(permission, PermissionManager.PERMISSION);
    }

    public static PermissionImpl setting(String permission) throws InvalidPermissionException {
        return fromId(permission, PermissionManager.SETTING);
    }

    public static PermissionImpl fromId(String permission) throws InvalidPermissionException {
        return fromId(permission, PermissionManager.COMBINED);
    }

    public static PermissionImpl fromId(String permission, RootNode rootNode) throws InvalidPermissionException {
        return new PermissionImpl(permission, rootNode);
    }

    public static PermissionImpl withNodes(Node... nodes) {
        if (nodes.length == 0) throw new IllegalArgumentException("Permissions needs at least 1 node");
        List<Node> list = new LinkedList<>();
        if (!(nodes[0] instanceof RootNode)) {
            list.add(PermissionManager.COMBINED);
        }
        list.addAll(Arrays.asList(nodes));
        return new PermissionImpl(list);
    }

    private List<Node> parse(RootNode rootNode) throws InvalidPermissionException {
        final List<Node> nodes = new LinkedList<>();
        String[] parts = permission.split(SEPARATOR_REGEX);
        Node currentNode = rootNode;
        nodes.add(rootNode);
        for (String part : parts) {
            Node node = currentNode.getNodesMap().get(part);
            if (node == null) throw new InvalidPermissionException("Couldn't find " + part + " node in " + currentNode.getName());
            nodes.add(node);
            currentNode = node;
        }
        return nodes;
    }

    @Override
    public boolean includes(Permission other) {
        List<Node> otherNodes = other.getNodes();
        for (int i = 0; i < this.nodes.size(); i++) {
            Node node = this.nodes.get(i);
            if (otherNodes.size() > i) {
                Node otherNode = otherNodes.get(i);
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
    public Permission withNode(Node node) {
        List<Node> nodes = new LinkedList<>(this.nodes);
        nodes.add(node);
        return new PermissionImpl(nodes);
    }

    @Override
    public void validateContext(Node.ChangeContext context) throws CommandSyntaxException {
        for (Node node : getNodes()) {
            if (!node.canChange(context)) throw PermissionArgument.FORBIDDEN;
        }
    }

    @Override
    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public Node getLastNode() {
        return nodes.get(nodes.size() - 1);
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
        return String.format("Permission[id=%s, nodes=%s]", permission, Arrays.toString(nodes.toArray()));
    }

    @Override
    public int hashCode() {
        return permission.hashCode();
    }
}
