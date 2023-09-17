package me.drex.itsours.gui.permission;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.context.GlobalContext;
import me.drex.itsours.claim.permission.node.ChildNode;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.claim.permission.util.Value;
import me.drex.itsours.claim.permission.visitor.PermissionVisitor;
import me.drex.itsours.command.SettingCommand;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.function.Predicate;

import static me.drex.message.api.LocalizedMessage.localized;

public class SettingsGui extends PermissionStorageGui {

    private final AbstractClaim claim;

    public SettingsGui(GuiContext context, AbstractClaim claim, Permission permission) {
        super(context, claim.getSettings(), permission);
        this.claim = claim;
        this.setTitle(localized("text.itsours.gui.settings.title", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(context.server()),
            Map.of("permission", Text.literal(permission.asString()))
        )));
    }

    @Override
    public Predicate<ChildNode> elementFilter() {
        return childNode -> childNode.canChange(new Node.ChangeContext(claim, GlobalContext.INSTANCE, Value.UNSET, context.player.getCommandSource()));
    }

    @Override
    Value getResult(Permission permission) {
        PermissionVisitor visitor = PermissionVisitor.create();
        claim.visit(null, permission, visitor);
        visitor.remove(claim, permission, GlobalContext.INSTANCE);
        return visitor.getResult();
    }

    @Override
    boolean setValue(Permission permission, Value value) {
        try {
            SettingCommand.INSTANCE.executeSet(player.getCommandSource().withSilent(), claim, permission, value);
            return true;
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    @Override
    PermissionStorageGui create(Permission permission) {
        return new SettingsGui(context, claim, permission);
    }

}
