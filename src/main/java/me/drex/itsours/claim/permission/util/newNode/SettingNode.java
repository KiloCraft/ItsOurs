package me.drex.itsours.claim.permission.util.newNode;

import me.drex.itsours.claim.permission.util.newNode.util.Node;

public class SettingNode extends Node {

    private boolean global = false;

    public SettingNode(String id) {
        super(id);
    }

    public SettingNode global() {
        this.global = true;
        return this;
    }
}
