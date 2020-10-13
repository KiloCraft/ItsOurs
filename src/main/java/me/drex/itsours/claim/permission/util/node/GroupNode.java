package me.drex.itsours.claim.permission.util.node;

public class GroupNode extends AbstractNode{

    private final String id;
    private final String[] permissions;

    public GroupNode(String id, String... permissions) {
        this.id = id;
        this.permissions = permissions;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public boolean contains(String node) {
        for (String permission : permissions) {
            if (node.equals(permission)) return true;
        }
        return false;
    }
}
