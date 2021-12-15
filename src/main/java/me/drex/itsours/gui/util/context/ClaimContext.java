package me.drex.itsours.gui.util.context;

import me.drex.itsours.claim.AbstractClaim;

public interface ClaimContext extends NoContext {

    public AbstractClaim getClaim();

}
