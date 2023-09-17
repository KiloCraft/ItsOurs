package me.drex.itsours.gui.permission;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.context.RoleContext;
import me.drex.itsours.claim.permission.node.ChildNode;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.claim.permission.util.Value;
import me.drex.itsours.claim.roles.Role;
import me.drex.itsours.command.RolesCommand;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.function.Predicate;

import static me.drex.message.api.LocalizedMessage.localized;

public class RoleStorageGui extends PermissionStorageGui {

    private final AbstractClaim claim;
    private final String roleId;
    private final Role role;
    private final RoleContext roleContext;

    public RoleStorageGui(GuiContext context, AbstractClaim claim, String roleId, Role role, Permission permission) {
        super(context, role.permissions(), permission);
        this.claim = claim;
        this.roleId = roleId;
        this.role = role;
        this.roleContext = claim.getRoleManager().createRoleContext(roleId, role);
        this.setTitle(localized("text.itsours.gui.role.title", PlaceholderUtil.mergePlaceholderMaps(
            Map.of("role_id", Text.literal(roleId)),
            Map.of("permission", Text.literal(permission.asString()))
        )));
    }

    @Override
    public Predicate<ChildNode> elementFilter() {
        return childNode -> childNode.canChange(new Node.ChangeContext(claim, roleContext, Value.UNSET, context.player.getCommandSource()));
    }

    @Override
    Value getResult(Permission permission) {
        // Unused, because nextValue is overwritten
        throw new UnsupportedOperationException();
    }

    @Override
    Value nextValue(Value value, Permission permission) {
        return value.next();
    }

    @Override
    boolean setValue(Permission permission, Value value) {
        try {
            RolesCommand.INSTANCE.setRolePermission(context.player.getCommandSource().withSilent(), claim, roleId, permission, value);
            return true;
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    @Override
    PermissionStorageGui create(Permission permission) {
        return new RoleStorageGui(context, claim, roleId, role, permission);
    }
}
