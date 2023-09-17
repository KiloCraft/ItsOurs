package me.drex.itsours.gui.claims;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.gui.GuiContext;

import java.util.Collection;

import static me.drex.message.api.LocalizedMessage.localized;

public class SubzoneListGui extends ClaimListGui<Subzone> {

    private final AbstractClaim parent;

    public SubzoneListGui(GuiContext context, AbstractClaim parent) {
        super(context);
        this.parent = parent;
        setTitle(localized("text.itsours.gui.claimlist.claim.title", parent.placeholders(context.server())));
    }

    @Override
    public Collection<Subzone> elements() {
        return parent.getSubzones();
    }
}
