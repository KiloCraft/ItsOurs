package me.drex.itsours.gui.claims;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.gui.permission.DefaultStorageGui;
import me.drex.itsours.util.PlaceholderUtil;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.item.Items;

import java.util.Collection;
import java.util.UUID;

import static me.drex.message.api.LocalizedMessage.localized;

public class PlayerClaimListGui extends ClaimListGui<Claim> {
    private final UUID uuid;

    public PlayerClaimListGui(GuiContext context, UUID uuid) {
        super(context);
        this.uuid = uuid;
        setTitle(localized("text.itsours.gui.claimlist.player.title", PlaceholderUtil.uuid("player_", uuid, context.server())));
    }

    @Override
    public Collection<Claim> elements() {
        return ClaimList.getClaimsFrom(uuid);
    }

    @Override
    public GuiElementBuilder buildNavigationBar(int index) {
        if (index == 7 && Permissions.check(context.player, "itsours.permissions.default", 2)) {
            return switchElement(Items.COOKED_BEEF, "claimlist.default", new DefaultStorageGui(context, Permission.permission()));
        }
        return super.buildNavigationBar(index);
    }
}
