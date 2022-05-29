package me.drex.itsours.claim.permission;

import me.drex.itsours.claim.permission.node.Node;

import java.util.List;

public interface Permission {

    boolean includes(Permission other);

    List<Node> getNodes();

    String asString();

}
