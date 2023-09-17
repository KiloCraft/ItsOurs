package me.drex.itsours.gui.permission;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.context.PersonalContext;
import me.drex.itsours.claim.permission.holder.PermissionData;
import me.drex.itsours.claim.permission.node.ChildNode;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.claim.permission.util.Value;
import me.drex.itsours.command.PermissionsCommand;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static me.drex.message.api.LocalizedMessage.localized;

public class PersonalStorageGui extends PermissionStorageGui {

    private final AbstractClaim claim;
    private final UUID player;

    public PersonalStorageGui(GuiContext context, AbstractClaim claim, UUID player, Permission permission) {
        super(context, claim.getPermissions().getOrDefault(player, new PermissionData()), permission);
        this.claim = claim;
        this.player = player;
        this.setTitle(localized("text.itsours.gui.personalstorage.title", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(context.server()),
            PlaceholderUtil.uuid("player_", player, context.server()),
            Map.of("permission", Text.literal(permission.asString()))
        )));
    }

    @Override
    public Predicate<ChildNode> elementFilter() {
        return childNode -> childNode.canChange(new Node.ChangeContext(claim, PersonalContext.INSTANCE, Value.UNSET, context.player.getCommandSource()));
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
            PermissionsCommand.INSTANCE.executeSet(context.player.getCommandSource().withSilent(), claim, Collections.singleton(new GameProfile(player, null)), permission, value);
            return true;
        } catch (CommandSyntaxException e) {
            return false;
        }
    }

    @Override
    PermissionStorageGui create(Permission permission) {
        return new PersonalStorageGui(context, claim, player, permission);
    }
}
