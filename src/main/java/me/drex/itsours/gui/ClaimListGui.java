package me.drex.itsours.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.util.Components;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ClaimListGui extends AbstractClaimListGui<Claim> {

    private final List<Claim> claims;

    public ClaimListGui(ServerPlayerEntity player, @Nullable SimpleGui previousGui, UUID owner) {
        super(player, previousGui, Text.translatable("text.itsours.gui.claimList.claims.title", Components.toText(owner)));
        claims = ClaimList.INSTANCE.getClaimsFrom(owner).stream().filter(claim -> claim instanceof Claim).map(claim -> (Claim) claim).toList();
    }

    @Override
    protected List<Claim> getElements() {
        return claims;
    }
}
