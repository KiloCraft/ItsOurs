package me.drex.itsours.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Subzone;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class SubzoneListGui extends AbstractClaimListGui<Subzone> {

    private final AbstractClaim parent;

    public SubzoneListGui(ServerPlayerEntity player, SimpleGui previousGui, AbstractClaim parent) {
        super(player, previousGui, Text.translatable("text.itsours.gui.claimList.subzones.title", parent.getFullName()));
        this.parent = parent;
    }

    @Override
    protected List<Subzone> getElements() {
        return parent.getSubzones();
    }

}
