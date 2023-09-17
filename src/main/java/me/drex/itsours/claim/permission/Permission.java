package me.drex.itsours.claim.permission;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import me.drex.itsours.claim.permission.node.ChildNode;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.claim.permission.node.RootNode;
import me.drex.itsours.claim.permission.util.InvalidPermissionException;

public interface Permission {

    Codec<Permission> CODEC = Codec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(valueOf(s));
        } catch (InvalidPermissionException e) {
            return DataResult.error(() -> s + " is not a valid permission.");
        }
    }, Permission::asString);

    static Permission valueOf(RootNode rootNode, String permission) throws InvalidPermissionException {
        return new PermissionImpl(rootNode, permission);
    }

    static Permission valueOf(String permission) throws InvalidPermissionException {
        return valueOf(PermissionManager.COMBINED, permission);
    }

    static Permission permission(RootNode rootNode, ChildNode... childNodes) {
        return new PermissionImpl(rootNode, childNodes);
    }

    static Permission permission(ChildNode... childNodes) {
        return permission(PermissionManager.COMBINED, childNodes);
    }

    boolean includes(Permission other);

    void validateContext(Node.ChangeContext context) throws CommandSyntaxException;

    Permission withNode(ChildNode node) throws InvalidPermissionException;

    ChildNode[] getChildNodes();

    Node getLastNode();

    String asString();

}
