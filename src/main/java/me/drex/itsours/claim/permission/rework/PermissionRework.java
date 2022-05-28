package me.drex.itsours.claim.permission.rework;

import me.drex.itsours.claim.permission.rework.node.Node;
import me.drex.itsours.claim.permission.rework.node.RootNode;
import me.drex.itsours.claim.permission.util.node.util.InvalidPermissionException;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class PermissionRework implements PermissionInterface {

    public static final String SEPARATOR = "\\.";

    private final String permission;
    private final List<Node> nodes;

    private PermissionRework(String permission, RootNode rootNode) throws InvalidPermissionException {
        this.permission = permission;
        this.nodes = parse(rootNode);
    }

    private PermissionRework(List<Node> nodes) {
        this.nodes = nodes;
        this.permission = String.join(SEPARATOR, nodes.stream().map(Node::getId).toList());
    }

    public static PermissionRework permission(String permission) throws InvalidPermissionException {
        return new PermissionRework(permission, PermissionManager.PERMISSION);
    }

    public static PermissionRework setting(String permission) throws InvalidPermissionException {
        return new PermissionRework(permission, PermissionManager.SETTING);
    }

    public static PermissionRework of(String permission) throws InvalidPermissionException {
        return new PermissionRework(permission, PermissionManager.COMBINED);
    }

    public static PermissionRework of(String permission, RootNode rootNode) throws InvalidPermissionException {
        return new PermissionRework(permission, rootNode);
    }

    public static PermissionRework of(List<Node> nodes) {
        return new PermissionRework(nodes);
    }

    private List<Node> parse(RootNode rootNode) throws InvalidPermissionException {
        final List<Node> nodes = new LinkedList<>();
        String[] parts = permission.split(SEPARATOR);
        Node currentNode = rootNode;
        for (String part : parts) {
            Node node = currentNode.getNodesMap().get(part);
            if (node == null) throw new InvalidPermissionException("Couldn't find " + part + " node in " + currentNode.getName());
            nodes.add(node);
            currentNode = node;
        }
        return nodes;
    }

    // TODO: remove old code
    /*private void parse() throws InvalidPermissionException {
        String[] parts = permission.split(SEPARATOR);
        Node currentNode = rootNode;
        for (String part : parts) {
            boolean found = false;
            for (Node node : currentNode.getNodes()) {
                if (node.getId().equals(part)) {
                    nodes.add(node);
                    currentNode = node;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new InvalidPermissionException("Couldn't find " + part + " node in " + currentNode.getName());
            }
        }
    }*/

    @Override
    public boolean includes(PermissionInterface other) {
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
    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public String asString() {
        return this.permission;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PermissionRework permission) {
            return this.permission.equals(permission.permission);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return permission.hashCode();
    }
}
