package me.drex.itsours.claim.permission.rework;

import me.drex.itsours.claim.permission.rework.node.Node;

import java.util.List;

public interface PermissionInterface {

    boolean includes(PermissionInterface other);

    List<Node> getNodes();

    String asString();

}
