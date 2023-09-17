package me.drex.itsours.gui.permission;

import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.util.Value;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.gui.GuiContext;
import net.minecraft.text.Text;

import java.util.Map;

import static me.drex.message.api.LocalizedMessage.localized;

public class DefaultStorageGui extends PermissionStorageGui {

    public DefaultStorageGui(GuiContext context, Permission permission) {
        super(context, DataManager.defaultSettings(), permission);
        this.setTitle(localized("text.itsours.gui.default.title", Map.of("permission", Text.literal(permission.asString()))));
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
        permissionData.set(permission, value);
        return true;
    }

    @Override
    PermissionStorageGui create(Permission permission) {
        return new DefaultStorageGui(context, permission);
    }
}
